package com.kuleuven.coverage;

import com.kuleuven.cfg.Generator;
import com.kuleuven.config.AppConfig;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfoByIdMap;
import com.kuleuven.coverage.CoverageAgent.shared.Out;
import com.kuleuven.coverage.CoverageAgent.util.CoverageCount;
import com.kuleuven.coverage.graph.CoverageGraph;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.util.DotExporter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.kuleuven.coverage.CoverageAgent.util.BlockInfoUtil.extractMethodSignatures;

public class GenerateCFGCoverageGraph {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: blockCoverageMapPath   (e.g., "out/coverage.out")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <blockCoverageMapPath>");
            System.exit(1);
        }

        String classPath = args[0];
        String blockCoverageMapPath = args[1];

        String blockMapPath = AppConfig.get("coverage.block_map.write.path");

        Map<Integer, BlockInfo> blockMap = null;

        try {
            blockMap = BlockInfoByIdMap.readFromJson(blockMapPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to load block map from path " + blockMapPath);
            System.exit(1);
        }

        try {
            List<int[]> executionPaths = Out.get(blockCoverageMapPath);

            Map<Integer, Integer> coverageCounts = CoverageCount.getByBlockId(executionPaths);

            Collection<String> fullyQualifiedMethodSignatures = extractMethodSignatures(blockMap.values());

            int i = 0;
            for (String methodSignature : fullyQualifiedMethodSignatures) {
                Generator generator = new Generator(classPath, methodSignature);
                ControlFlowGraph<?> cfg = generator.getCfg();

                CoverageGraph coverageGraph = new CoverageGraph(cfg, blockMap, coverageCounts);

                writeOutputs(coverageGraph, i);
                i++;
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to read block coverage map from path " + blockCoverageMapPath);
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Failed to deserialize block coverage map: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void writeOutputs(CoverageGraph coverageGraph, int i) throws IOException {
        String outputPathPrefix = AppConfig.get("coverage.graph.write.path_prefix");
        String ext = AppConfig.get("coverage.graph.write.extension");
        String outputPathString = String.format("%s%s%s", outputPathPrefix, i, ext);

        Path outputPath = Path.of(outputPathString);
        Files.createDirectories(outputPath.getParent());

        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write(coverageGraph.getGraph().toDotGraph());
            System.out.println("✅ DOT file written to " + outputPath);
        }
    }
}
