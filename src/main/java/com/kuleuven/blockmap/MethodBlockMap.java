package com.kuleuven.blockmap;

import com.kuleuven.blockmap.model.BlockDataDTO;
import com.kuleuven.blockmap.model.MethodBlockMapDTO;
import com.kuleuven.coverage.model.LineDTO;

import java.util.ArrayList;
import java.util.List;

public class MethodBlockMap {

    public static List<LineDTO> getLineCoverage(MethodBlockMapDTO methodBlockMap) {
        List<LineDTO> lineCoverage = new ArrayList<>();

        for (BlockDataDTO block : methodBlockMap.blocks) {
            if (block.coverageData != null && block.coverageData.lines != null) {
                lineCoverage.addAll(block.coverageData.lines);
            }
        }

        return lineCoverage;
    }
}
