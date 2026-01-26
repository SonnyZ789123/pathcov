package com.kuleuven.blockmap;

import java.util.List;

public class BlockDataDTO {
    private final int id;
    private final String sourceHash;
    private final BlockCoverageDataDTO coverageData;
    private final List<Integer> parentBlockId;
    private final List<Integer> successorBlockIds;

    public BlockDataDTO(
            int id,
            String sourceHash,
            BlockCoverageDataDTO coverageData,
            List<Integer> predecessorBlockIds,
            List<Integer> successorBlockIds) {
        this.id = id;
        this.sourceHash = sourceHash;
        this.coverageData = coverageData;
        this.parentBlockId = predecessorBlockIds;
        this.successorBlockIds = successorBlockIds;
    }

    public int getId() {
        return id;
    }

    public String getSourceHash() {
        return sourceHash;
    }

    public BlockCoverageDataDTO getCoverageData() {
        return coverageData;
    }

    public List<Integer> getParentBlockId() {
        return parentBlockId;
    }

    public List<Integer> getSuccessorBlockIds() {
        return successorBlockIds;
    }
}
