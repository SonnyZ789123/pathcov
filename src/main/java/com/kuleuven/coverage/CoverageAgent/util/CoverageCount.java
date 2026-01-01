package com.kuleuven.coverage.CoverageAgent.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CoverageCount {
    public static Map<Integer, Integer> getByBlockId(List<int[]> blockPaths) {
        Map<Integer, Integer> coverageCounts = new HashMap<>();
        // Per execution path, count each blockId only once to remove duplicates due to loops
        for (int[] path : blockPaths) {
            Set<Integer> seenInPath = new HashSet<>();
            for (int blockId : path) {
                if (seenInPath.add(blockId)) {
                    coverageCounts.merge(blockId, 1, Integer::sum);
                }
            }
        }
        return coverageCounts;
    }
}
