package com.kuleuven.blockmap;

import com.kuleuven.blockmap.hash.BlockHashBuilder;
import com.kuleuven.coverage.CoverageReport;
import com.kuleuven.coverage.model.LineDTO;
import com.kuleuven.coverage.model.MethodDTO;
import com.kuleuven.jvm.descriptor.SootMethodEncoder;
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
    private final Map<Integer, LineDTO> lineToCoverageMap;

    public BlockMapBuilder(Map<SootMethod, ControlFlowGraph<?>> methodToCfgMap, CoverageReport coverageReport) {
        this.methodToCfgMap = methodToCfgMap;
        this.coverageReport = coverageReport;
        this.lineToCoverageMap = coverageReport.getLineToCoverageMap();
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

            BlockHashBuilder blockHashBuilder = new BlockHashBuilder(block);
            String sourceHash = blockHashBuilder.build();

            BlockCoverageDataDTO blockCoverageData;

            if (methodCoverage != null) {
                List<LineDTO> lineCoverageList = getLineCoverageList(block);

                // No line coverage for a block is considered as uncovered.
                // Could be a fully synthetic block without any source mapping?
                blockCoverageData = lineCoverageList.isEmpty() ?
                        BlockCoverageDataDTO.createNoCoverageData() : new BlockCoverageDataDTO(lineCoverageList);
            } else {
                blockCoverageData = BlockCoverageDataDTO.createNoCoverageData();
            }

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
