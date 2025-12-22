package com.kuleuven.coverage.CoverageAgent.shared;

public record BlockInfo(
        int blockId,
        String className,
        String methodName,
        String methodDescriptor,
        String stmtId,
        int lineNumber
) {}
