package com.kuleuven.blockmap;

import java.util.List;

public class BlockMapDTO {

    private final List<MethodBlockMapDTO> methodBlockMaps;

    public BlockMapDTO(List<MethodBlockMapDTO> methodBlockMaps) {
        this.methodBlockMaps = methodBlockMaps;
    }

    public List<MethodBlockMapDTO> getMethodBlockMaps() {
        return methodBlockMaps;
    }
}
