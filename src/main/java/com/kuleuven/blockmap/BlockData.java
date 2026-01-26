package com.kuleuven.blockmap;

import java.util.List;

public class BlockData {
    private final int id;
    private final String sourceHash;
    private final BlockCoverageData coverageData;
    private final List<Integer> parentBlockId;
    private final List<Integer> successorBlockIds;

    public BlockData(
            int id,
            String sourceHash,
            BlockCoverageData coverageData,
            List<Integer> predecessorBlockIds,
            List<Integer> successorBlockIds) {
        this.id = id;
        this.sourceHash = sourceHash;
        this.coverageData = coverageData;
        this.parentBlockId = predecessorBlockIds;
        this.successorBlockIds = successorBlockIds;
    }
}
