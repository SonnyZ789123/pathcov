package com.kuleuven.coverage;

import com.kuleuven.config.AppConfig;
import com.kuleuven.icfg.CoverageAgent.shared.BlockInfoByIdMap;
import com.kuleuven.coverage.CoverageAgent.shared.Out;
import com.kuleuven.coverage.CoverageAgent.util.CoverageCount;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import com.kuleuven.icfg.Generator;
import com.kuleuven.icfg.coverage.CoverageBlockInfo;
import com.kuleuven.icfg.sootup.analysis.interprocedural.icfg.BuildICFGGraph;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GenerateICFGCoverageGraph {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         *   2: blockCoverageMapPath   (e.g., "out/coverage.json")
         *   3: blockMapPath           (e.g., "./output/block_map.json")
         */
        if (args.length < 3) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature> <blockCoverageMapPath> [blockMapPath]");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];
        String blockCoverageMapPath = args[2];
        String blockMapPath = args.length >= 4 ? args[3] : null;

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

            Map<Integer, CoverageBlockInfo> coverageBlockMap = new HashMap<>();
            for (Map.Entry<Integer, BlockInfo> entry : blockMap.entrySet()) {
                Integer blockId = entry.getKey();
                BlockInfo info = entry.getValue();
                Integer count = coverageCounts.getOrDefault(blockId, 0);
                coverageBlockMap.put(blockId, new CoverageBlockInfo(info, count));
            }

            Generator generator = new Generator(classPath, fullyQualifiedMethodSignature);
            JimpleBasedInterproceduralCFG icfg = generator.getICfg();

            BuildICFGGraph builder = new BuildICFGGraph(generator.view, icfg, coverageBlockMap);
            String icfgAsDot = builder.buildICFGGraph();

            writeOutputs(icfgAsDot);
        } catch (IOException e) {
            System.err.println("❌ Failed to read block coverage map from path " + blockCoverageMapPath);
            System.exit(1);
        }
    }

    /**
     * Writes the interprocedural control flow graph's DOT representation.
     *
     * @param icfgAsDot The interprocedural control flow graph to write
     * @throws IOException If writing fails
     */
    private static void writeOutputs(String icfgAsDot) throws IOException {

        String cfgPath = AppConfig.get("coverage.icfg.write.path");

        Path output = Path.of(cfgPath);
        Files.createDirectories(output.getParent());

        try (FileWriter writer = new FileWriter(output.toFile())) {
            writer.write(icfgAsDot);
            System.out.println("✅ ICFG coverage DOT file written to " + output);
        }
    }
}
