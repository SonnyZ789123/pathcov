package com.kuleuven.blockmap;

import com.kuleuven.coverage.model.LineDTO;

import java.util.ArrayList;
import java.util.List;

public class MethodBlockMapDTO {

    private final String fullName;
    private final List<BlockDataDTO> blocks;

    public MethodBlockMapDTO(String fullName, List<BlockDataDTO> blocks) {
        this.fullName = fullName;
        this.blocks = blocks;
    }

    public String getFullName() {
        return fullName;
    }

    public List<BlockDataDTO> getBlocks() {
        return blocks;
    }

    public List<LineDTO> getLineCoverage() {
        List<LineDTO> lineCoverage = new ArrayList<>();

        for (BlockDataDTO block : blocks) {
            if (block.getCoverageData() != null && block.getCoverageData().getLines() != null) {
                lineCoverage.addAll(block.getCoverageData().getLines());
            }
        }

        return lineCoverage;
    }
}
