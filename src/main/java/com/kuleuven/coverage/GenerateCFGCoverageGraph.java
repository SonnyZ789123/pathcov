package com.kuleuven.coverage;

import com.kuleuven.cfg.Generator;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfoByIdMap;
import com.kuleuven.coverage.CoverageAgent.shared.Out;
import com.kuleuven.coverage.CoverageAgent.util.CoverageCount;
import com.kuleuven.coverage.graph.CoverageGraph;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import sootup.core.graph.ControlFlowGraph;

import java.io.*;
import java.util.*;

import static com.kuleuven.coverage.CoverageAgent.util.BlockInfoUtil.extractMethodSignatures;

public class GenerateCFGCoverageGraph {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: blockMapPath           (e.g., "out/cfg_block_map.json")
         *   2: blockCoverageMapPath   (e.g., "out/coverage.out")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <blockMapPath> <blockCoverageMapPath>");
            System.exit(1);
        }

        String classPath = args[0];
        String blockMapPath = args[1];
        String blockCoverageMapPath = args[2];

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
        // Ensure output directory exists
        (new File("out")).mkdirs();

        // Write DOT graph representation
        String filename = String.format("out/cfg_coverage%s.dot", i);
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(coverageGraph.getGraph().toDotGraph());
            System.out.println("✅ DOT file written to " + filename);
        }
    }
}
