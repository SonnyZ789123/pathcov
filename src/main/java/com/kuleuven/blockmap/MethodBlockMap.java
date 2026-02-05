package com.kuleuven.blockmap;

import com.kuleuven.blockmap.model.BlockDataDTO;
import com.kuleuven.blockmap.model.MethodBlockMapDTO;
import com.kuleuven.coverage.model.LineDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodBlockMap {

    private final MethodBlockMapDTO methodBlockMap;

    private final Map<String, BlockDataDTO> hashToBlockDataMap;

    public MethodBlockMap(MethodBlockMapDTO methodBlockMap) {
        this.methodBlockMap = methodBlockMap;
        this.hashToBlockDataMap = new HashMap<>();

        for (BlockDataDTO block : methodBlockMap.blocks) {
            hashToBlockDataMap.put(block.sourceHash, block);
        }
    }

    public BlockDataDTO getBlockDataByHash(String blockHash) {
        return hashToBlockDataMap.get(blockHash);
    }

    public List<LineDTO> getLineCoverage() {
        List<LineDTO> lineCoverage = new ArrayList<>();

        for (BlockDataDTO block : methodBlockMap.blocks) {
            if (block.coverageData != null && block.coverageData.lines != null) {
                lineCoverage.addAll(block.coverageData.lines);
            }
        }

        return lineCoverage;
    }
}
