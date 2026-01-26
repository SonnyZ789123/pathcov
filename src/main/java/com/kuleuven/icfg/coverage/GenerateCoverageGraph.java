package com.kuleuven.icfg.coverage;

import com.github.javaparser.quality.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.blockmap.BlockMapDTO;
import com.kuleuven.config.AppConfig;
import com.kuleuven.icfg.Generator;
import com.kuleuven.icfg.sootup.analysis.interprocedural.icfg.BuildICFGGraph;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GenerateCoverageGraph {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         *   2: blockMapPath           (e.g., "./out/icfg_block_map.json")
         *   3: outputPath             (e.g., "/data/coverage_graph.dot")
         *   4: projectPrefixes        (e.g., "com.kuleuven,test.SimpleExample")
         */
        if (args.length < 3) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature> <blockMapPath> [outputPath] [projectPrefixes]");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];
        String blockMapPath = args[2];
        String outputPath = args.length >= 4 ? args[3] : null;
        List<String> projectPrefixes = args.length >= 5
                ? List.of(args[4].split(","))
                : null;

        BlockMapDTO blockMap = null;
        try (FileReader reader = new FileReader(Path.of(blockMapPath).toFile())) {
            Gson gson = new GsonBuilder().create();
             blockMap = gson.fromJson(reader, BlockMapDTO.class);
        } catch (IOException e) {
            System.err.println("❌ Failed to read block map from path " + blockMapPath);
            System.exit(1);
        }

        try {
            Generator generator = new Generator(classPath, fullyQualifiedMethodSignature, projectPrefixes);
            JimpleBasedInterproceduralCFG icfg = generator.getICfg();

            BuildICFGGraph builder = new BuildICFGGraph(generator.getView(), icfg, new BlockCoverageMap(blockMap));
            String icfgAsDot = builder.buildICFGGraph(true);

            writeOutputs(icfgAsDot, outputPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to write coverage graph: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Writes the interprocedural control flow graph's DOT representation.
     *
     * @param icfgAsDot The interprocedural control flow graph to write
     * @param outputPath The optional output path
     * @throws IOException If writing fails
     */
    private static void writeOutputs(String icfgAsDot, @Nullable String outputPath) throws IOException {
        String writeOutputPath = outputPath != null ? outputPath : AppConfig.get("coverage.icfg.write.path");

        Path output = Path.of(writeOutputPath);
        Files.createDirectories(output.getParent());

        try (FileWriter writer = new FileWriter(output.toFile())) {
            writer.write(icfgAsDot);
            System.out.println("✅ ICFG coverage DOT file written to " + output);
        }
    }
}
