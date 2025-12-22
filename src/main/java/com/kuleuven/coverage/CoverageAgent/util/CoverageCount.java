package com.kuleuven.coverage.CoverageAgent.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CoverageCount {
    public static Map<Integer, Integer> getByBlockId(List<int[]> executionPaths) {
        Map<Integer, Integer> coverageCounts = new HashMap<>();
        for (int[] path : executionPaths) {
            for (int blockId : path) {
                coverageCounts.merge(blockId, 1, Integer::sum);
            }
        }
        return coverageCounts;
    }
}
