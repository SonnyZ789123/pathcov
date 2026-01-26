package com.kuleuven.blockmap;

import com.kuleuven.coverage.model.LineDTO;

import java.util.Collections;
import java.util.List;

public class BlockCoverageDataDTO {
    public enum CoverageState {
        COVERED,
        NOT_COVERED,
        PARTIALLY_COVERED
    }

    private final List<LineDTO> lines;
    private final CoverageState coverageState;

    public BlockCoverageDataDTO(List<LineDTO> lines) {
        this.lines = lines;
        this.coverageState = determineCoverageState(lines);
    }

    private BlockCoverageDataDTO(List<LineDTO> lines, CoverageState coverageState) {
        this.lines = lines;
        this.coverageState = coverageState;
    }

    public List<LineDTO> getLines() {
        return lines;
    }

    public CoverageState getCoverageState() {
        return coverageState;
    }

    public static BlockCoverageDataDTO createEmpty() {
        return new BlockCoverageDataDTO(Collections.emptyList(), CoverageState.NOT_COVERED);
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
