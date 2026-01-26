package com.kuleuven.blockmap;

import com.kuleuven.coverage.CoverageReport;
import com.kuleuven.coverage.model.LineDTO;
import com.kuleuven.coverage.model.MethodDTO;
import com.kuleuven.jvm.descriptor.SootMethodEncoder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

import java.util.*;

public class BlockMapBuilder {

    private final Map<SootMethod, ControlFlowGraph<?>> methodToCfgMap;
    private final CoverageReport coverageReport;
    private final Map<BasicBlock<?>, Integer> blockToIdMap = new HashMap<>();

    public BlockMapBuilder(Map<SootMethod, ControlFlowGraph<?>> methodToCfgMap, CoverageReport coverageReport) {
        this.methodToCfgMap = methodToCfgMap;
        this.coverageReport = coverageReport;
    }

    public BlockMapDTO build() {
        initBlockToIdMap();

        List<MethodBlockMapDTO> methodBlockMaps = new ArrayList<>();
        for (Map.Entry<SootMethod, ControlFlowGraph<?>> entry : methodToCfgMap.entrySet()) {
            SootMethod method = entry.getKey();
            ControlFlowGraph<?> cfg = entry.getValue();

            String jvmFullName = SootMethodEncoder.toJvmMethodFullName(method.getSignature().toString());
            MethodDTO methodCoverage = coverageReport.getForMethodFullName(jvmFullName);

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
            // TODO: Compute actual source hash based on the actual source.
            String sourceHash = String.valueOf(block.hashCode());
            BlockCoverageDataDTO blockCoverageData = methodCoverage != null ?
                    new BlockCoverageDataDTO(getLineCoverageList(block, methodCoverage)) :
                    BlockCoverageDataDTO.createEmpty();

            List<? extends BasicBlock<?>> parentBlocks = block.getPredecessors();
            List<? extends BasicBlock<?>> successorBlocks = block.getSuccessors();

            List<Integer> parentBlockIds = parentBlocks.stream()
                    .map(blockToIdMap::get).filter(Objects::nonNull).toList();
            List<Integer> successorBlockIds = successorBlocks.stream()
                    .map(blockToIdMap::get).filter(Objects::nonNull).toList();

            BlockDataDTO blockData = new BlockDataDTO(
                    blockId,
                    sourceHash,
                    blockCoverageData,
                    parentBlockIds,
                    successorBlockIds);
            blockDataList.add(blockData);
        }

        return blockDataList;
    }

    private List<LineDTO> getLineCoverageList(BasicBlock<?> block, @NonNull MethodDTO methodCoverage) {
        List<LineDTO> lineCoverageList = new ArrayList<>();

        // Line with the lowest line number gets polled first
        Queue<LineDTO> methodCoverageLines = new PriorityQueue<>(Comparator.comparingInt(a -> a.line));
        methodCoverageLines.addAll(methodCoverage.lines);

        Queue<Stmt> stmts = new PriorityQueue<>(
                Comparator.comparingInt(stmt ->
                        stmt.getPositionInfo().getStmtPosition().getFirstLine()
                )
        );

        block.getStmts().stream()
                // Filter out synthetic statements without position info (e.g., @caughtexception)
                .filter(stmt -> stmt.getPositionInfo() != null)
                .filter(stmt -> stmt.getPositionInfo().getStmtPosition() != null)
                .filter(stmt -> stmt.getPositionInfo().getStmtPosition().getFirstLine() > 0)
                .forEach(stmts::add);

        if (stmts.isEmpty()) {
            return lineCoverageList;
        }

        Stmt firstStmt = stmts.peek();
        assert firstStmt != null; // no empty blocks

        int firstStmtLine = firstStmt.getPositionInfo().getStmtPosition().getFirstLine();

        // Remove all lines that are before the first statement in the block
        while (!methodCoverageLines.isEmpty() && methodCoverageLines.peek().line < firstStmtLine) {
            methodCoverageLines.remove();
        }

        while (!stmts.isEmpty() && !methodCoverageLines.isEmpty()) {
            Stmt stmt = stmts.remove();

            int firstLine = stmt.getPositionInfo().getStmtPosition().getFirstLine();
            int lastLine = stmt.getPositionInfo().getStmtPosition().getLastLine();

            LineDTO lineCoverage = methodCoverageLines.peek();
            assert lineCoverage != null;
            // TODO: we could also just add any lines that fall within the block's head's first line, and tail's last line
            // but that does not check that every line is in a corresponding statement.
            if (lineCoverage.line < firstLine) {
                throw new IllegalStateException("❌ Coverage line " + lineCoverage.line +
                        " does not match any statement in the block");
            }

            // Add all lines that fall within the statement's line range
            while (!methodCoverageLines.isEmpty() && methodCoverageLines.peek().line <= lastLine) {
                LineDTO matched = methodCoverageLines.remove();
                lineCoverageList.add(matched);
            }
        }

        return lineCoverageList;
    }
}
