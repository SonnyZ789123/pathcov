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

import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.ControlFlowGraphNode;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;

public class CoverageEdge extends PropertyGraphEdge {
    private final AbstCfgEdge internalCfgEdge;
    private final int successorIndex;

    public CoverageEdge(CoverageNode sourceNode, CoverageNode destinationNode, AbstCfgEdge internalCfgEdge, int successorIndex) {
        super(sourceNode, destinationNode);

        this.internalCfgEdge = internalCfgEdge;
        this.successorIndex = successorIndex;
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

    public int getSuccessorIndex() {
        return successorIndex;
    }

    /**
     * Returns the branch index that is compatible and shared with JDart representation.
     *
     * @return the branch index compatible with JDart
     */
    public int getBranchIndex() {
        if (this.internalCfgEdge instanceof IfTrueCfgEdge) {
            return 0;
        } else if (this.internalCfgEdge instanceof IfFalseCfgEdge) {
            return 1;
        } else {
            return getSuccessorIndex();
        }
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
