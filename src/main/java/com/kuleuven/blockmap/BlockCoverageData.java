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
