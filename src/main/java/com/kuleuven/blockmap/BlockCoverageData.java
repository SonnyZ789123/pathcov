package com.kuleuven.blockmap;

import com.kuleuven.coverage.model.LineDTO;

import java.util.Collections;
import java.util.List;

public class BlockCoverageData {
    public enum CoverageState {
        COVERED,
        NOT_COVERED,
        PARTIALLY_COVERED
    }

    private final List<LineDTO> lines;
    private final CoverageState coverageState;

    public BlockCoverageData(List<LineDTO> lines) {
        this.lines = lines;
        this.coverageState = determineCoverageState(lines);
    }

    private BlockCoverageData(List<LineDTO> lines,CoverageState coverageState) {
        this.lines = lines;
        this.coverageState = coverageState;
    }

    public static BlockCoverageData createEmpty() {
        return new BlockCoverageData(Collections.emptyList(), CoverageState.NOT_COVERED);
    }

    private CoverageState determineCoverageState(List<LineDTO> lineCoverageList) {
        boolean partiallyCovered = false;

        for (LineDTO lineCoverage : lineCoverageList) {
            if (lineCoverage.hits > 0) {
                partiallyCovered = true;

                // Support branch coverage
                if (lineCoverage.branches.covered < lineCoverage.branches.total) {
                    return CoverageState.PARTIALLY_COVERED;
                }
            }

            if (lineCoverage.hits == 0) {
                if (partiallyCovered) {
                    return CoverageState.PARTIALLY_COVERED;
                }
            }
        }

        return partiallyCovered ? CoverageState.COVERED : CoverageState.NOT_COVERED;
    }
}
