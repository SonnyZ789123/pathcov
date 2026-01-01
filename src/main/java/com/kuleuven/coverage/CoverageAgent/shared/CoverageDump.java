package com.kuleuven.coverage.CoverageAgent.shared;

import java.util.List;

public final class CoverageDump {
    public final int version;
    public final List<CoveragePath> paths;

    public CoverageDump(int version, List<CoveragePath> paths) {
        this.version = version;
        this.paths = paths;
    }
}
