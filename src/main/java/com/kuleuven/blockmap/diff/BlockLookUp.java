/*
 * Copyright (c) 2025-2026 Yoran Mertens
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
