package com.kuleuven.coverage.CoverageAgent.shared;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Out {
    private final List<CoveragePath> coveragePaths;
    private final Set<String> methodFullNames;

    public Out(String outputPath) throws IOException, ClassNotFoundException {
        Gson gson = new Gson();

        try (Reader r = Files.newBufferedReader(Path.of(outputPath))) {
            CoverageDump dump = gson.fromJson(r, CoverageDump.class);
            this.coveragePaths = dump.paths;
            this.methodFullNames = extractMethodFullNames();
        }
    }

    private Set<String> extractMethodFullNames() {
        Set<String> methodFullNames = new HashSet<>();
        for (CoveragePath coveragePath : coveragePaths) {
            methodFullNames.add(coveragePath.methodFullName);
        }
        return methodFullNames;
    }

    public Set<String> getMethodFullNames() {
        return methodFullNames;
    }

    /**
     * Block ids are unique across methods, so putting all block paths together is fine.
     *
     * @return List of block paths.
     */
    public List<int[]> getBlockPaths() {
        List<int[]> blockPaths = new ArrayList<>();
        coveragePaths.forEach(coveragePath -> {
            blockPaths.add(coveragePath.blockPath);
        });
        return blockPaths;
    }

    public List<int[]> getInstructionPaths(String methodFullName) {
        List<int[]> instructionPaths = new ArrayList<>();
        coveragePaths.forEach(coveragePath -> {
            if (coveragePath.methodFullName.equals(methodFullName)) {
                instructionPaths.add(coveragePath.instructionPath);
            }
        });
        return instructionPaths;
    }
}
