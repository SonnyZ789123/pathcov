package com.kuleuven.icfg.coverage;

import com.github.javaparser.quality.Nullable;
import com.kuleuven.cg.SootUpCGWrapper;
import com.kuleuven.config.AppConfig;
import com.kuleuven.coverage.intellij.shared.CoverageDataReader;
import com.kuleuven.icfg.CoverageAgent.shared.BlockInfoByIdMap;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
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
         *   2: coverageDataPath       (e.g., "out/coverage.json")
         *   3: blockMapPath           (e.g., "./output/block_map.json")
         *   4: outputPath             (e.g., "/data/coverage_graph.dot")
         *   5: projectPrefixes        (e.g., "com.kuleuven,test.SimpleExample")
         */
        if (args.length < 3) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature> <coverageDataPath> [blockMapPath] [outputPath] [projectPrefixes]");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];
        String coverageDataPath = args[2];
        String blockMapPath = args.length >= 4 ? args[3] : null;
        String outputPath = args.length >= 5 ? args[4] : null;
        List<String> projectPrefixes = args.length >= 6
                ? List.of(args[5].split(","))
                : null;

        Map<Integer, BlockInfo> blockMap = null;
        try {
            blockMap = BlockInfoByIdMap.readFromJson(blockMapPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to load block map: " + e.getMessage());
            System.exit(1);
        }

        try {
            CoverageDataReader reader = new CoverageDataReader(coverageDataPath);
            Generator generator = new Generator(classPath, fullyQualifiedMethodSignature, projectPrefixes);
            JimpleBasedInterproceduralCFG icfg = generator.getICfg();

            // Filter only project classes in ICFG
            SootUpCGWrapper cgWrapper = new SootUpCGWrapper(icfg.getCg(), projectPrefixes);

            BuildICFGGraph builder = new BuildICFGGraph(
                    generator.getView(), icfg, cgWrapper, reader.getCoverageReport());
            String icfgAsDot = builder.buildICFGGraph(true);

            writeOutputs(icfgAsDot, outputPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to read block coverage map from path " + coverageDataPath);
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
