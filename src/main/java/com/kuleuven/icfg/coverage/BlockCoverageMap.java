package com.kuleuven.icfg.coverage;

import com.kuleuven.blockmap.model.BlockMapDTO;
import com.kuleuven.blockmap.model.MethodBlockMapDTO;
import org.jspecify.annotations.Nullable;

public class BlockCoverageMap {

    private final BlockMapDTO blockMap;

    public BlockCoverageMap(BlockMapDTO blockMap) {
        this.blockMap = blockMap;
    }

    public @Nullable MethodBlockMapDTO getForMethodFullName(String methodFullName) {
        return blockMap.methodBlockMaps.stream()
                .filter(m -> m.fullName.equals(methodFullName))
                .findFirst().orElse(null);
    }
}
