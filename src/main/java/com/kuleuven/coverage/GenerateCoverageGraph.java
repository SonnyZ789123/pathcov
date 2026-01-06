package com.kuleuven.coverage;

import com.kuleuven.cfg.Generator;
import com.kuleuven.config.AppConfig;
import com.kuleuven.icfg.CoverageAgent.shared.BlockInfoByIdMap;
import com.kuleuven.coverage.CoverageAgent.shared.Out;
import com.kuleuven.coverage.CoverageAgent.util.CoverageCount;
import com.kuleuven.coverage.graph.CoverageGraph;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import sootup.core.graph.ControlFlowGraph;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.kuleuven.coverage.CoverageAgent.util.BlockInfoUtil.extractMethodSignatures;

public class GenerateCoverageGraph {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: blockCoverageMapPath   (e.g., "out/coverage.out")
         *   2: blockMapPath           (e.g., "./output/block_map.json")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <blockCoverageMapPath> [blockMapPath]");
            System.exit(1);
        }

        String classPath = args[0];
        String blockCoverageMapPath = args[1];
        String blockMapPath = args.length >= 3 ? args[2] : null;

        Map<Integer, BlockInfo> blockMap = null;

        try {
            blockMap = BlockInfoByIdMap.readFromJson(blockMapPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to load block map: " + e.getMessage());
            System.exit(1);
        }

        try {
            Out out = new Out(blockCoverageMapPath);
            List<int[]> blockPaths = out.getBlockPaths();

            Map<Integer, Integer> coverageCounts = CoverageCount.getByBlockId(blockPaths);

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
            System.out.println("✅ Coverage graph DOT file written to " + outputPath);
        }

        String rankingOutputPathPrefix = AppConfig.get("coverage.graph.jdart.write.path_prefix");
        String rankingExt = AppConfig.get("coverage.graph.jdart.write.extension");
        String rankingOutputPathString = String.format("%s%s%s", rankingOutputPathPrefix, i, rankingExt);

        Path rankingOutputPath = Path.of(rankingOutputPathString);
        Files.createDirectories(rankingOutputPath.getParent());

        try (FileWriter writer = new FileWriter(rankingOutputPath.toFile())) {
            writer.write(coverageGraph.toJson());
            System.out.println("✅ Coverage graph JSON file written to " + rankingOutputPath);
        }
    }
}
