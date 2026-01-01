package com.kuleuven.coverage.CoverageAgent.shared;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Out {
    private final List<CoveragePath> coveragePaths;

    public Out(String outputPath) throws IOException, ClassNotFoundException {
        Gson gson = new Gson();

        try (Reader r = Files.newBufferedReader(Path.of(outputPath))) {
            CoverageDump dump = gson.fromJson(r, CoverageDump.class);
            this.coveragePaths = dump.paths;
        }
    }

    public List<CoveragePath> get() {
        return coveragePaths;
    }

    public List<int[]> getBlockPaths() {
        List<int[]> blockPaths = new ArrayList<>();
        for (CoveragePath coveragePath : coveragePaths) {
            blockPaths.add(coveragePath.blockPath);
        }
        return blockPaths;
    }

    public List<int[]> getInstructionPaths() {
        List<int[]> instructionPaths = new ArrayList<>();
        for (CoveragePath coveragePath : coveragePaths) {
            instructionPaths.add(coveragePath.instructionPath);
        }
        return instructionPaths;
    }
}
