package com.kuleuven.jdart;

import com.kuleuven.cfg.Generator;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfoByIdMap;
import com.kuleuven.coverage.CoverageAgent.shared.Out;
import com.kuleuven.coverage.CoverageAgent.util.CoverageCount;
import com.kuleuven.coverage.graph.CoverageGraph;
import sootup.core.graph.ControlFlowGraph;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.kuleuven.coverage.CoverageAgent.util.BlockInfoUtil.extractMethodSignatures;

public class Main {
    public static void main(String[] args) {
        String classPath = "/Users/yoran/dev/jdart-examples/out/production/jdart-examples";
        String blockCoverageMapPath = "/Users/yoran/dev/master-thesis/data/coverage.out";
        String blockMapPath = null;

        Map<Integer, BlockInfo> blockMap = null;
        try {
            blockMap = BlockInfoByIdMap.readFromJson(blockMapPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to load block map: " + e.getMessage());
            System.exit(1);
        }

        try {
            Out out = new Out(blockCoverageMapPath);
            List<int[]> executionPaths = out.getBlockPaths();

            Map<Integer, Integer> coverageCounts = CoverageCount.getByBlockId(executionPaths);

            Collection<String> fullyQualifiedMethodSignatures = extractMethodSignatures(blockMap.values());
            String methodSignature = fullyQualifiedMethodSignatures.iterator().next();

            Generator generator = new Generator(classPath, methodSignature);
            ControlFlowGraph<?> cfg = generator.getCfg();

            CoverageGraph coverageGraph = new CoverageGraph(cfg, blockMap, coverageCounts);
    } catch (IOException e) {
            System.err.println("❌ Failed to read block coverage map from path " + blockCoverageMapPath);
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Failed to deserialize block coverage map: " + e.getMessage());
            System.exit(1);
        }
    }
}
