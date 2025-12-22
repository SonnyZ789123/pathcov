package com.kuleuven.coverage.graph;

import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import com.kuleuven.coverage.CoverageAgent.StmtId;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.common.stmt.Stmt;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CoverageGraph extends MutableBlockControlFlowGraph {
    private final ControlFlowGraph<?> cfg;
    private final Map<Integer, Integer> blockIdToCoverageCount;
    private final Map<Integer, BlockInfo> blocksById;
    private final PropertyGraph graph;
    private final Map<String, BlockInfo> blockInfoByStmtId = new LinkedHashMap<>();

    public CoverageGraph(
            ControlFlowGraph<?> cfg,
            Map<Integer, BlockInfo> blocksById,
            Map<Integer, Integer> blockIdToCoverageCount) {
        this.cfg = cfg;
        this.blocksById = blocksById;
        this.blockIdToCoverageCount = blockIdToCoverageCount;

        fillBlockInfoByStmtId();

        this.graph = createGraph(cfg);
    }

    public PropertyGraph getGraph() {
        return graph;
    }

    private void fillBlockInfoByStmtId() {
        blocksById.values().forEach(blockInfo -> {
            blockInfoByStmtId.putIfAbsent(blockInfo.stmtId(), blockInfo);
        });
    }

    private BlockInfo findBlockInfoByStmt(Stmt stmt) {
        String stmtId = StmtId.getStmtId(stmt);
        return blockInfoByStmtId.get(stmtId);
    }

    /**
     * Reference: See CfgCreator class of SootUp 2.0.
     * Creates the coverage graph for the given Soot method.
     *
     * @param cfg the ControlFlowGraph
     * @return the coverage graph
     */
    private PropertyGraph createGraph(ControlFlowGraph<?> cfg) {
        PropertyGraph.Builder graphBuilder = new CFGCoverageGraph.Builder();
        graphBuilder.setName("cfg_coverage");

        // (blockId, CoverageNode)
        Map<Integer, CoverageNode> seenBlocks = new HashMap<>();

        cfg.getBlocks().forEach(
            currBlock -> {
                Stmt entryStmt = currBlock.getHead();
                Stmt tailStmt = currBlock.getTail();

                BlockInfo blockInfo = findBlockInfoByStmt(entryStmt);
                if (blockInfo == null) {
                    return;
                }
                CoverageNode sourceBlockNode = seenBlocks.get(blockInfo.blockId());
                if (sourceBlockNode == null) {
                    sourceBlockNode = new CoverageNode(
                            blockInfo,
                            currBlock,
                            blockIdToCoverageCount.getOrDefault(blockInfo.blockId(), 0));
                    seenBlocks.put(blockInfo.blockId(), sourceBlockNode);
                    graphBuilder.addNode(sourceBlockNode);
                }

                int expectedCount = tailStmt.getExpectedSuccessorCount();
                int successorIndex = 0;

                for (Stmt successor : cfg.getAllSuccessors(tailStmt)) {
                    BlockInfo successorBlockInfo = findBlockInfoByStmt(successor);
                    if (successorBlockInfo == null) {
                        continue;
                    }
                    CoverageNode destinationBlockNode = seenBlocks.get(successorBlockInfo.blockId());
                    if (destinationBlockNode == null) {
                        destinationBlockNode = new CoverageNode(
                                successorBlockInfo,
                                currBlock,
                                blockIdToCoverageCount.getOrDefault(successorBlockInfo.blockId(), 0));
                        seenBlocks.put(successorBlockInfo.blockId(), destinationBlockNode);
                        graphBuilder.addNode(destinationBlockNode);
                    }
                    CoverageEdge edge = CoverageEdge.of(tailStmt, successorIndex, sourceBlockNode, destinationBlockNode);

                    if (successorIndex >= expectedCount) {
                        edge = CoverageEdge.exceptionalEdge(sourceBlockNode, destinationBlockNode);
                    }

                    graphBuilder.addEdge(edge);
                    successorIndex++;
                }
            });

        return graphBuilder.build();
    }
}
