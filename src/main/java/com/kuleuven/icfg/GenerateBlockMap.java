package com.kuleuven.icfg;

import com.github.javaparser.quality.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.blockmap.BlockMapDTO;
import com.kuleuven.blockmap.BlockMapGenerator;
import com.kuleuven.config.AppConfig;
import com.kuleuven.coverage.intellij.shared.CoverageDataReader;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GenerateBlockMap {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         *   2: coverageDataPath       (e.g., "out/coverage_data.json")
         *   3: outputPath             (e.g., "/data/blockmap.json")
         *   4: projectPrefixes        (e.g., "com.kuleuven,test.SimpleExample")
         */
        if (args.length < 3) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature> <coverageDataPath> [outputPath] [projectPrefixes]");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];
        String coverageDataPath = args[2];
        String outputPath = args.length >= 4 ? args[3] : null;
        List<String> projectPrefixes = args.length >= 5
                ? List.of(args[4].split(","))
                : null;

        try {
            CoverageDataReader reader = new CoverageDataReader(coverageDataPath);
            Generator generator = new Generator(classPath, fullyQualifiedMethodSignature, projectPrefixes);
            JimpleBasedInterproceduralCFG icfg = generator.getICfg();

            BlockMapDTO blockMap = BlockMapGenerator.generateBlockMap(
                    generator.getView(), icfg, reader.getCoverageReport());

            writeOutputs(blockMap, outputPath);
        } catch (IOException e) {
            System.err.println("❌ Error while generating block map: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void writeOutputs(BlockMapDTO blockMap, @Nullable String outputPath) throws IOException {
        String writeOutputPath = outputPath != null
                ? outputPath
                : AppConfig.get("icfg.block_map.write.path");

        Path output = Path.of(writeOutputPath);
        Files.createDirectories(output.getParent());

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        try (FileWriter writer = new FileWriter(output.toFile())) {
            gson.toJson(blockMap, writer);
            System.out.println("✅ Block map JSON written to " + output);
        }
    }
}
