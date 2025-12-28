package com.kuleuven.coverage.graph;

import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.ControlFlowGraphNode;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;

public class CoverageEdge extends PropertyGraphEdge {
    private final AbstCfgEdge internalCfgEdge;
    private final int branchIndex;

    public CoverageEdge(CoverageNode sourceNode, CoverageNode destinationNode, AbstCfgEdge internalCfgEdge, int branchIndex) {
        super(sourceNode, destinationNode);

        this.internalCfgEdge = internalCfgEdge;
        this.branchIndex = branchIndex;
    }

    @Override public CoverageNode getSource() {
        return (CoverageNode) super.getSource();
    }

    @Override public CoverageNode getDestination() {
        return (CoverageNode) super.getDestination();
    }

    @Override
    public String getLabel() {
        return this.internalCfgEdge.getLabel();
    }

    public int getBranchIndex() {
        return branchIndex;
    }

    public static CoverageEdge exceptionalEdge(
            CoverageNode sourceNode,
            CoverageNode destinationNode) {
        ControlFlowGraphNode source = new ControlFlowGraphNode(sourceNode.getBlock().getTail());
        ControlFlowGraphNode destination = new ControlFlowGraphNode(destinationNode.getBlock().getHead());

        AbstCfgEdge internalEdge = new ExceptionalCfgEdge(source, destination);

        return new CoverageEdge(sourceNode, destinationNode, internalEdge, -1);
    }

    public static CoverageEdge of(
            Stmt currStmt,
            int successorIndex,
            CoverageNode sourceNode,
            CoverageNode destinationNode) {
        ControlFlowGraphNode source = new ControlFlowGraphNode(sourceNode.getBlock().getTail());
        ControlFlowGraphNode destination = new ControlFlowGraphNode(destinationNode.getBlock().getHead());

        AbstCfgEdge internalEdge = createEdge(
                currStmt,
                successorIndex,
                source,
                destination);

        return new CoverageEdge(sourceNode, destinationNode, internalEdge, successorIndex);
    }

    /**
     * Reference: See CfgCreator class of SootUp 2.0.
     * Creates an edge between the source and destination nodes based on the type of statement.
     *
     * @param currStmt the current statement
     * @param successorIndex the index of the successor
     * @param sourceNode the source node
     * @param destinationNode the destination node
     * @return the created edge
     */
    private static AbstCfgEdge createEdge(
            Stmt currStmt, int successorIndex, ControlFlowGraphNode sourceNode, ControlFlowGraphNode destinationNode) {
        if (currStmt instanceof JIfStmt) {
            return successorIndex == JIfStmt.TRUE_BRANCH_IDX
                    ? new IfTrueCfgEdge(sourceNode, destinationNode)
                    : new IfFalseCfgEdge(sourceNode, destinationNode);
        } else if (currStmt instanceof JSwitchStmt) {
            return new SwitchCfgEdge(sourceNode, destinationNode);
        } else if (currStmt instanceof JGotoStmt) {
            return new GotoCfgEdge(sourceNode, destinationNode);
        } else {
            return new NormalCfgEdge(sourceNode, destinationNode);
        }
    }
}
