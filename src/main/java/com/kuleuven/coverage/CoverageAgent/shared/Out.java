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

package com.kuleuven.coverage.CoverageAgent.shared;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class Out {
    private final List<ExecutionCoveragePath> coveragePaths;
    private final Set<String> methodFullNames;

    public Out(String outputPath) throws IOException {
        Gson gson = new Gson();

        try (Reader r = Files.newBufferedReader(Path.of(outputPath))) {
            CoverageDump dump = gson.fromJson(r, CoverageDump.class);
            this.coveragePaths = dump.executionPaths;
            this.methodFullNames = extractMethodFullNames();
        }
    }

    private Set<String> extractMethodFullNames() {
        Set<String> methodFullNames = new HashSet<>();
        forEachCoveragePath(coveragePath -> {
            methodFullNames.add(coveragePath.methodFullName);
        });
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
        forEachCoveragePath(coveragePath -> {
            blockPaths.add(coveragePath.blockPath);
        });
        return blockPaths;
    }

    public List<int[]> getInstructionPaths(String methodFullName) {
        List<int[]> instructionPaths = new ArrayList<>();
        forEachCoveragePath(coveragePath -> {
            if (coveragePath.methodFullName.equals(methodFullName)) {
                instructionPaths.add(coveragePath.instructionPath);
            }
        });
        return instructionPaths;
    }

    // Currently we don't have any use case for knowing the entry method, so we flatmap it.
    private void forEachCoveragePath(Consumer<CoveragePath> consumer) {
        coveragePaths.forEach(executionCoveragePath -> {
            executionCoveragePath.coveragePaths.forEach(consumer);
        });
    }
}
