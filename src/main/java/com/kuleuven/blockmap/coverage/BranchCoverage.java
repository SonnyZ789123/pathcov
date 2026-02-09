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
