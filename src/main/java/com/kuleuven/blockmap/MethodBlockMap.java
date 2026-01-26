package com.kuleuven.blockmap;

import java.util.List;

public class MethodBlockMap {

    private final String fullName;
    private final List<BlockData> blocks;

    public MethodBlockMap(String fullName, List<BlockData> blocks) {
        this.fullName = fullName;
        this.blocks = blocks;
    }
}
