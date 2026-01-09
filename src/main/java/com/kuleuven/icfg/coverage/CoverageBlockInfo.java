package com.kuleuven.icfg.coverage;

import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;

public record CoverageBlockInfo(BlockInfo blockInfo, Integer coverageCount) {
}
