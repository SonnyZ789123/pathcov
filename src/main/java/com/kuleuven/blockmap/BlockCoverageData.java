package com.kuleuven.blockmap;

import com.kuleuven.blockmap.model.BlockCoverageDataDTO;
import com.kuleuven.blockmap.model.BlockCoverageDataDTO.CoverageState;
import com.kuleuven.coverage.model.LineDTO;

import java.util.Collections;
import java.util.List;

public class BlockCoverageData {

    public static BlockCoverageDataDTO createBlockCoverageDataDTO(List<LineDTO> lines) {
        return new BlockCoverageDataDTO(lines, determineCoverageState(lines));
    }

    public static BlockCoverageDataDTO createNoCoverageData() {
        return new BlockCoverageDataDTO(Collections.emptyList(), CoverageState.NOT_COVERED);
    }

    private static CoverageState determineCoverageState(List<LineDTO> lineCoverageList) {
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
