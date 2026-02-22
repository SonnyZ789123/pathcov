package com.kuleuven.blockmap.diff;

import com.kuleuven.blockmap.model.BlockDataDTO;
import com.kuleuven.blockmap.model.BlockMapDTO;
import com.kuleuven.blockmap.model.MethodBlockMapDTO;

import java.util.*;

public class BlockLookUp {

    private final BlockMapDTO blockMap;
    private final Map<Integer, BlockDataDTO> idToBlockDataMap = new HashMap<>();
    /** Multiple blocks can have the same hash if they are structurally identical */
    private final Map<String, List<BlockDataDTO>> hashToBlockDataMap = new HashMap<>();

    public BlockLookUp(BlockMapDTO blockMap) {
        this.blockMap = blockMap;

        for (MethodBlockMapDTO methodBlockMap : blockMap.methodBlockMaps) {
            for (BlockDataDTO blockData : methodBlockMap.blocks) {
                idToBlockDataMap.put(blockData.id, blockData);
                hashToBlockDataMap
                        .computeIfAbsent(blockData.blockHash, k -> new ArrayList<>())
                        .add(blockData);
            }
        }
    }

    public BlockDataDTO getBlockDataById(int blockId) {
        return idToBlockDataMap.get(blockId);
    }

    public List<BlockDataDTO> getBlocksByHash(String hash) {
        return hashToBlockDataMap.getOrDefault(hash, Collections.emptyList());
    }

    public Collection<BlockDataDTO> getAllBlocks() {
        return idToBlockDataMap.values();
    }
}
