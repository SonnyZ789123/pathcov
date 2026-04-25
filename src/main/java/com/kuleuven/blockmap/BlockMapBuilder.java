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

import com.kuleuven.blockmap.hash.BlockHashBuilder;
import com.kuleuven.blockmap.model.*;
import com.kuleuven.coverage.CoverageReport;
import com.kuleuven.coverage.model.*;
import com.kuleuven.jvm.descriptor.SootMethodEncoder;
import org.jspecify.annotations.Nullable;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.SootMethod;

import java.util.*;

public class BlockMapBuilder {

    private final Map<SootMethod, ControlFlowGraph<?>> methodToCfgMap;
    private final @org.jetbrains.annotations.Nullable CoverageReport coverageReport;
    private final Map<BasicBlock<?>, Integer> blockToIdMap = new HashMap<>();
    private final Map<Integer, LineDTO> lineToCoverageMap;

    public BlockMapBuilder(Map<SootMethod, ControlFlowGraph<?>> methodToCfgMap, @Nullable CoverageReport coverageReport) {
        this.methodToCfgMap = methodToCfgMap;
        this.coverageReport = coverageReport;
        if (coverageReport != null) {
            this.lineToCoverageMap = coverageReport.getLineToCoverageMap();
        } else {
            this.lineToCoverageMap = new HashMap<>();
        }
    }

    public BlockMapDTO build() {
        initBlockToIdMap();

        List<MethodBlockMapDTO> methodBlockMaps = new ArrayList<>();
        for (Map.Entry<SootMethod, ControlFlowGraph<?>> entry : methodToCfgMap.entrySet()) {
            SootMethod method = entry.getKey();
            ControlFlowGraph<?> cfg = entry.getValue();

            String jvmFullName = SootMethodEncoder.toJvmMethodFullName(method.getSignature().toString());
            MethodDTO methodCoverage = null;
            if (coverageReport != null) {
                methodCoverage = coverageReport.getForMethodFullName(jvmFullName);
            }

            List<BlockDataDTO> methodBlockData = buildBlockMapForMethod(cfg, methodCoverage);
            methodBlockMaps.add(new MethodBlockMapDTO(jvmFullName, methodBlockData));
        }

        return new BlockMapDTO(methodBlockMaps);
    }

    private void initBlockToIdMap() {
        int blockIdCounter = 0;

        for (ControlFlowGraph<?> cfg : methodToCfgMap.values()) {
            for (BasicBlock<?> block : cfg.getBlocks()) {
                if (!blockToIdMap.containsKey(block)) {
                    blockToIdMap.put(block, blockIdCounter++);
                }
            }
        }
    }

    private List<BlockDataDTO> buildBlockMapForMethod(ControlFlowGraph<?> cfg, @Nullable MethodDTO methodCoverage) {
        List<BlockDataDTO> blockDataList = new ArrayList<>();

        for (BasicBlock<?> block : cfg.getBlocks()) {
            int blockId = blockToIdMap.get(block);
            assert blockId != -1;

            BlockHashBuilder blockHashBuilder = new BlockHashBuilder(block);
            String blockHash = blockHashBuilder.build();

            BlockCoverageDataDTO blockCoverageData;

            if (methodCoverage != null) {
                List<LineDTO> lineCoverageList = getLineCoverageList(block);

                // No line coverage for a block is considered as uncovered.
                // Could be a fully synthetic block without any source mapping?
                blockCoverageData = lineCoverageList.isEmpty() ?
                        BlockCoverageData.createNoCoverageData() :
                        BlockCoverageData.createBlockCoverageDataDTO(lineCoverageList);
            } else {
                blockCoverageData = BlockCoverageData.createNoCoverageData();
            }

            List<? extends BasicBlock<?>> parentBlocks = block.getPredecessors();
            List<? extends BasicBlock<?>> successorBlocks = block.getSuccessors();

            List<Integer> parentBlockIds = parentBlocks.stream()
                    .map(blockToIdMap::get).filter(Objects::nonNull).toList();
            List<Integer> successorBlockIds = successorBlocks.stream()
                    .map(blockToIdMap::get).filter(Objects::nonNull).toList();

            List<EdgeCoverageDTO> edges = buildEdgeCoverage(block, cfg);

            BlockDataDTO blockData = new BlockDataDTO(
                    blockId,
                    blockHash,
                    blockCoverageData,
                    parentBlockIds,
                    successorBlockIds,
                    edges);
            blockDataList.add(blockData);
        }

        return blockDataList;
    }

    // region Edge coverage

    private List<EdgeCoverageDTO> buildEdgeCoverage(BasicBlock<?> block, ControlFlowGraph<?> cfg) {
        List<? extends BasicBlock<?>> successors = block.getSuccessors();
        if (successors.isEmpty()) {
            return Collections.emptyList();
        }

        Stmt tailStmt = block.getTail();
        List<EdgeCoverageDTO> edges = new ArrayList<>();

        for (int successorIndex = 0; successorIndex < successors.size(); successorIndex++) {
            BasicBlock<?> successor = successors.get(successorIndex);
            Integer targetBlockId = blockToIdMap.get(successor);
            if (targetBlockId == null) continue;

            BranchType branchType = determineBranchType(tailStmt, successorIndex);
            int branchIndex = determineBranchIndex(tailStmt, successorIndex);
            int hits = resolveEdgeHits(block, tailStmt, successorIndex, branchType, cfg);
            Integer switchCaseKey = resolveSwitchCaseKey(tailStmt, successorIndex);

            edges.add(new EdgeCoverageDTO(targetBlockId, branchIndex, branchType, hits, switchCaseKey));
        }

        return edges;
    }

    /**
     * Determines the branch type based on the tail statement and successor index.
     * Reuses the same logic as {@code CoverageEdge.createEdge()}.
     */
    private BranchType determineBranchType(Stmt tailStmt, int successorIndex) {
        if (tailStmt instanceof JIfStmt) {
            return successorIndex == JIfStmt.TRUE_BRANCH_IDX
                    ? BranchType.IF_TRUE
                    : BranchType.IF_FALSE;
        } else if (tailStmt instanceof JSwitchStmt switchStmt) {
            return successorIndex >= switchStmt.getValues().size()
                    ? BranchType.SWITCH_DEFAULT
                    : BranchType.SWITCH_CASE;
        } else if (tailStmt instanceof JGotoStmt) {
            return BranchType.GOTO;
        } else {
            return BranchType.NORMAL;
        }
    }

    /**
     * Determines the JDart-compatible branch index.
     * Reuses the same logic as {@code CoverageEdge.getBranchIndex()}.
     */
    private int determineBranchIndex(Stmt tailStmt, int successorIndex) {
        if (tailStmt instanceof JIfStmt) {
            return successorIndex == JIfStmt.TRUE_BRANCH_IDX ? 0 : 1;
        } else {
            return successorIndex;
        }
    }

    /**
     * Resolves the hit count for a specific edge from IntelliJ coverage data.
     * Returns -1 when coverage data is unavailable.
     */
    private int resolveEdgeHits(BasicBlock<?> block, Stmt tailStmt, int successorIndex, BranchType branchType, ControlFlowGraph<?> cfg) {
        if (tailStmt.getPositionInfo() == null) return -1;

        int tailLine = tailStmt.getPositionInfo().getStmtPosition().getFirstLine();
        if (tailLine <= 0) return -1;

        LineDTO lineCoverage = lineToCoverageMap.get(tailLine);
        if (lineCoverage == null) return -1;

        if (tailStmt instanceof JIfStmt) {
            return resolveIfEdgeHits(block, tailLine, lineCoverage, successorIndex, cfg);
        } else if (tailStmt instanceof JSwitchStmt switchStmt) {
            return resolveSwitchEdgeHits(switchStmt, lineCoverage, successorIndex);
        } else {
            // NORMAL or GOTO: if the line was hit, the single outgoing edge was taken
            return lineCoverage.hits;
        }
    }

    /**
     * Resolves hit count for an IF branch edge.
     * Handles disambiguation when multiple JIfStmts share the same source line (e.g. {@code if (a && b)}).
     *
     * Note: IntelliJ's trueBranch/falseBranch refers to the bytecode condition, which is always
     * negated compared to the source code (Java compiles {@code if (cond)} as {@code if (!cond) goto else}).
     * SootUp re-negates back to source semantics, so SootUp's TRUE branch = IntelliJ's falseBranch
     * and SootUp's FALSE branch = IntelliJ's trueBranch.
     */
    private int resolveIfEdgeHits(BasicBlock<?> block, int tailLine, LineDTO lineCoverage, int successorIndex, ControlFlowGraph<?> cfg) {
        if (lineCoverage.jumps.isEmpty()) {
            return -1;
        }

        int jumpIndex = computeJumpIndexForBlock(block, tailLine, cfg);

        if (jumpIndex < 0 || jumpIndex >= lineCoverage.jumps.size()) {
            return -1;
        }

        JumpDTO jump = lineCoverage.jumps.get(jumpIndex);

        if (successorIndex == JIfStmt.TRUE_BRANCH_IDX) {
            return jump.falseBranch.hits;
        } else {
            return jump.trueBranch.hits;
        }
    }

    /**
     * Computes which JumpDTO index corresponds to this block's JIfStmt,
     * by counting JIfStmt-tail blocks on the same source line in CFG iteration order.
     */
    private int computeJumpIndexForBlock(BasicBlock<?> targetBlock, int targetLine, ControlFlowGraph<?> cfg) {
        int index = 0;
        for (BasicBlock<?> b : cfg.getBlocks()) {
            if (b == targetBlock) {
                return index;
            }
            Stmt tail = b.getTail();
            if (tail instanceof JIfStmt
                    && tail.getPositionInfo() != null
                    && tail.getPositionInfo().getStmtPosition().getFirstLine() == targetLine) {
                index++;
            }
        }
        return -1;
    }

    /**
     * Resolves hit count for a SWITCH branch edge.
     */
    private int resolveSwitchEdgeHits(JSwitchStmt switchStmt, LineDTO lineCoverage, int successorIndex) {
        if (lineCoverage.switches.isEmpty()) {
            return -1;
        }

        SwitchDTO switchData = lineCoverage.switches.get(0);
        int numCases = switchStmt.getValues().size();

        if (successorIndex >= numCases) {
            // Default branch
            return switchData.defaultBranch.hits;
        } else if (successorIndex < switchData.cases.size()) {
            return switchData.cases.get(successorIndex).hits;
        } else {
            return -1;
        }
    }

    /**
     * Returns the switch case key for SWITCH_CASE edges, null otherwise.
     */
    private @Nullable Integer resolveSwitchCaseKey(Stmt tailStmt, int successorIndex) {
        if (!(tailStmt instanceof JSwitchStmt switchStmt)) {
            return null;
        }
        if (successorIndex >= switchStmt.getValues().size()) {
            return null; // Default branch has no case key
        }
        return switchStmt.getValues().get(successorIndex).getValue();
    }

    // endregion

    private List<LineDTO> getLineCoverageList(BasicBlock<?> block) {
        List<Stmt> stmts = block.getStmts().stream()
                // Filter out synthetic statements without position info (e.g., @caughtexception)
                .filter(stmt -> stmt.getPositionInfo() != null)
                .filter(stmt -> stmt.getPositionInfo().getStmtPosition().getFirstLine() > 0)
                .toList();

        if (stmts.isEmpty()) {
            return Collections.emptyList();
        }

        return stmts.stream()
                .map(stmt -> lineToCoverageMap.get(stmt.getPositionInfo().getStmtPosition().getFirstLine()))
                // You can still have statements without line coverage info (e.g., stmts that correspond to a catch(...) line)
                .filter(Objects::nonNull)
                .toList();
    }
}
