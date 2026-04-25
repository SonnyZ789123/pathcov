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

package com.kuleuven.blockmap.coverage;

import com.kuleuven.blockmap.model.BlockDataDTO;
import com.kuleuven.blockmap.model.BlockMapDTO;
import com.kuleuven.blockmap.model.MethodBlockMapDTO;

public class BranchCoverage {
    public static double calculate(BlockMapDTO blockMap) {
        int totalCovered = blockMap.methodBlockMaps.stream()
                .flatMap(method -> method.blocks.stream())
                .flatMap(block -> block.coverageData.lines.stream())
                .mapToInt(line -> line.branches.covered)
                .sum();

        int totalBranches = blockMap.methodBlockMaps.stream()
                .flatMap(method -> method.blocks.stream())
                .flatMap(block -> block.coverageData.lines.stream())
                .mapToInt(line -> line.branches.total)
                .sum();

        return calculate(totalCovered, totalBranches);
    }

    public static double calculate(MethodBlockMapDTO methodBlockMap) {
        int totalCovered = methodBlockMap.blocks.stream()
                .flatMap(block -> block.coverageData.lines.stream())
                .mapToInt(line -> line.branches.covered)
                .sum();

        int totalBranches = methodBlockMap.blocks.stream()
                .flatMap(block -> block.coverageData.lines.stream())
                .mapToInt(line -> line.branches.total)
                .sum();

        return calculate(totalCovered, totalBranches);
    }

    public static double calculate(BlockDataDTO blockData) {
        int totalCovered = blockData.coverageData.lines.stream()
                .mapToInt(line -> line.branches.covered)
                .sum();

        int totalBranches = blockData.coverageData.lines.stream()
                .mapToInt(line -> line.branches.total)
                .sum();

        return calculate(totalCovered, totalBranches);
    }

    /**
     * Calculates branch coverage percentage based on covered and total branches.
     *
     * @param coveredBranches Number of covered branches
     * @param totalBranches Total number of branches
     * @return Branch coverage percentage (0.0 to 100.0)
     */
    public static double calculate(int coveredBranches, int totalBranches) {
        if (totalBranches == 0) {
            return 100.0; // If there are no branches, we consider it fully covered
        }
        return (double) coveredBranches / totalBranches * 100.0;
    }

}
