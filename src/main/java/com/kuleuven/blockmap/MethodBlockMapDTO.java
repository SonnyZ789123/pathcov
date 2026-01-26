package com.kuleuven.blockmap;

import java.util.List;

public class MethodBlockMapDTO {

    private final String fullName;
    private final List<BlockDataDTO> blocks;

    public MethodBlockMapDTO(String fullName, List<BlockDataDTO> blocks) {
        this.fullName = fullName;
        this.blocks = blocks;
    }
}
