package com.kuleuven.icfg.coverage;

import com.kuleuven.blockmap.BlockMapDTO;
import com.kuleuven.blockmap.MethodBlockMapDTO;
import org.jspecify.annotations.Nullable;

public class BlockCoverageMap {

    private final BlockMapDTO blockMap;

    public BlockCoverageMap(BlockMapDTO blockMap) {
        this.blockMap = blockMap;
    }

    public @Nullable MethodBlockMapDTO getForMethodFullName(String methodFullName) {
        return blockMap.getMethodBlockMaps().stream()
                .filter(m -> m.getFullName().equals(methodFullName))
                .findFirst().orElse(null);
    }
}
