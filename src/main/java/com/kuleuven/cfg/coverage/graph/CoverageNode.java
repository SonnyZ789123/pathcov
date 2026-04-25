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

package com.kuleuven.cfg.coverage.graph;

import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import org.checkerframework.checker.nullness.qual.NonNull;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;
import sootup.core.graph.BasicBlock;

import java.util.Objects;

public class CoverageNode extends PropertyGraphNode {
    private final BlockInfo blockInfo;
    private final BasicBlock<?> block;
    private final int coverageCount;

    public CoverageNode(@NonNull BlockInfo blockInfo, @NonNull BasicBlock<?> block, int coverageCount) {
        this.blockInfo = blockInfo;
        this.block = block;
        this.coverageCount = coverageCount;
    }

    public BlockInfo getBlockInfo() {
        return blockInfo;
    }

    public BasicBlock<?> getBlock() {
        return block;
    }

    public int getCoverageCount() {
        return coverageCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoverageNode that = (CoverageNode) o;
        return blockInfo.equals(that.getBlockInfo()) &&
                coverageCount == that.getCoverageCount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockInfo, block);
    }

    @Override
    public String toString() {
        return blockInfo.className() + "." +
                blockInfo.methodName() + blockInfo.methodDescriptor() + " " +
                blockInfo.stmtId();
    }
}
