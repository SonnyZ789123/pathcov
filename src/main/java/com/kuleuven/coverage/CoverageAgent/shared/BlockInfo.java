package com.kuleuven.coverage.CoverageAgent.shared;

public record BlockInfo(
        // The unique identifier of the block, a block id is unique across multiple methods.
        int blockId,
        String className,
        String methodName,
        String methodDescriptor,
        String stmtId,
        int lineNumber
) {
    @Override

    public String toString() {
        return "BlockInfo{" +
                "blockId=" + blockId +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodDescriptor='" + methodDescriptor + '\'' +
                ", stmtId='" + stmtId + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
