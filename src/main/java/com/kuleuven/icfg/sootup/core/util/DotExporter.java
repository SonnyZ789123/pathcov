package com.kuleuven.icfg.sootup.core.util;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.google.common.collect.Sets;

import java.util.*;

import com.kuleuven.blockmap.MethodBlockMap;
import com.kuleuven.blockmap.hash.BlockHashBuilder;
import com.kuleuven.blockmap.model.BlockDataDTO;
import com.kuleuven.coverage.model.LineDTO;
import org.apache.commons.text.StringEscapeUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

/**
 * Exports a ControlFlowGraph into a Dot representation (see https://graphviz.org) to visualize the
 * Graph.
 * <p>
 * Slightly modified version of SootUp's v2.0.1 DotExporter to support weighted nodes in ICFGs
 *
 * @author Markus Schmidt, Yoran Mertens
 */
public class DotExporter {
    private enum CoverageType {
        FULL,
        PARTIAL,
        NONE,
        MISSING
    }

    public static String buildGraph(
            @NonNull ControlFlowGraph<?> graph,
            Map<Integer, MethodSignature> calls,
            MethodSignature methodSignature,
            Set<MethodSignature> methodSignatures,
            boolean compact,
            @Nullable MethodBlockMap methodBlockMap) {

        // TODO: hint: use edge weight to have a better top->down code like linear layouting with
        // starting stmt at the top;
        // TODO: improvement: use dfs starting with startingstmt to have a more intuitive order of
        // blocks

        StringBuilder sb = new StringBuilder();

        boolean isAdded = false;

        /* entrypoint */
        Stmt startingStmt = graph.getStartingStmt();

        /* print a block in a subgraph */
        int i = 0;

        Collection<? extends BasicBlock<?>> blocks;
        try {
            blocks = graph.getBlocksSorted();
        } catch (Exception e) {
            blocks = graph.getBlocks();
        }

        Set<BasicBlock<?>> drawnBlocks = Sets.newHashSetWithExpectedSize(blocks.size());

        for (BasicBlock<?> block : blocks) {
            BlockHashBuilder blockHashBuilder = new BlockHashBuilder(block);
            String blockHash = blockHashBuilder.build();

            StringBuilder coverageLabelSb = new StringBuilder();
            StringBuilder coverageStyleSb = new StringBuilder();
            if (methodBlockMap != null) {
                BlockDataDTO blockData = methodBlockMap.getBlockDataByHash(blockHash);
                CoverageType blockCoverageType = getBlockCoverageType(blockData);
                coverageLabelSb
                        .append("\t\tlabel = \"").append(getBlockLabel(blockData)).append("\"\n");
                coverageStyleSb
                        .append("\t\tstyle = filled\n")
                        .append("\t\tcolor = ").append(getBlockColor(blockCoverageType)).append("\n")
                        .append("\t\tfontsize = 10\n");
            }

            sb.append("//  lines [")
                    .append(block.getHead().getPositionInfo().getStmtPosition().getFirstLine())
                    .append(": ")
                    .append(block.getTail().getPositionInfo().getStmtPosition().getFirstLine())
                    .append("] \n");

            sb.append("\tsubgraph cluster_")
                    .append(block.hashCode())
                    .append(" { \n")
                    .append(coverageLabelSb)
                    .append(coverageStyleSb);

            /* print stmts in a block*/
            drawnBlocks.add(block);

            List<Stmt> stmts = block.getStmts();
            List<Stmt> shownStmts = new ArrayList<>();
            if (!compact) { // show all stmts
                stmts.forEach(stmt -> {
                    sb.append(createIternalBlockStmt(stmt, stmt.equals(startingStmt)));
                    shownStmts.add(stmt);
                });
            } else { // show only head, tail and calls to other methods
                for (Stmt stmt : stmts) {
                    boolean invokesOtherMethod = calls.entrySet().stream()
                            .filter(stmtToMethodSignature ->
                                    methodSignatures.contains(stmtToMethodSignature.getValue()))
                            .anyMatch(stmtToMethodSignature ->
                                    stmtToMethodSignature.getKey().equals(stmt.hashCode()));

                    if (block.getHead().equals(stmt) || block.getTail().equals(stmt) || invokesOtherMethod) {
                        sb.append(createIternalBlockStmt(stmt, stmt.equals(startingStmt)));
                        shownStmts.add(stmt);
                    }
                }
            }

            // add blocks internal connection
            if (stmts.size() > 1) {
                sb.append("\n\t\t");
                for (Stmt stmt : shownStmts) {
                    for (Map.Entry<Integer, MethodSignature> entry : calls.entrySet()) {
                        int stmtHashCode = entry.getKey();
                        MethodSignature targetMethodSignature = entry.getValue();
                        if (methodSignature.equals(targetMethodSignature) && !isAdded) {
                            sb.append(stmtHashCode).append(" -> ");
                            isAdded = true;
                        }
                    }
                    sb.append(stmt.hashCode()).append(" -> ");
                }
                sb.delete(sb.length() - 4, sb.length());
                sb.append("\n");
            }
            sb.append("\t}\n");

            /* add edges to other blocks */
            List<? extends BasicBlock<?>> successors = block.getSuccessors();
            if (successors.size() > 0) {
                Stmt tailStmt = block.getTail();

                Iterator<String> labelIt;
                // build edge labels for branching stmts
                if (tailStmt instanceof BranchingStmt) {
                    if (tailStmt instanceof JIfStmt) {
                        labelIt = Arrays.asList("false", "true").iterator();
                    } else if (tailStmt instanceof JSwitchStmt) {
                        labelIt =
                                ((JSwitchStmt) tailStmt).getValues().stream().map(s -> "case " + s).iterator();
                    } else {
                        labelIt = Collections.emptyIterator();
                    }
                } else {
                    labelIt = Collections.emptyIterator();
                }

                for (BasicBlock<?> successorBlock : successors) {
                    sb.append("\t").append(tailStmt.hashCode());
                    final boolean successorIsAlreadyDrawn = drawnBlocks.contains(successorBlock);
                    if (successorIsAlreadyDrawn) {
                        sb.append(":e -> ");
                    } else {
                        sb.append(":s -> ");
                    }
                    sb.append(successorBlock.getHead().hashCode()).append(":n");

                    if (labelIt.hasNext()) {
                        sb.append("[");
                        if (labelIt.hasNext()) {
                            sb.append("label=\"").append(labelIt.next()).append("\"");
                        }
                        sb.append("]");
                    }
                    //          sb.append("ltail=\"cluster_").append(block.hashCode()).append("\",
                    // lhead=\"cluster_").append(successorBlock.hashCode()).append("\"]");
                    sb.append("\n");
                }
            }

            /* add exceptional edges */
            Map<? extends ClassType, ? extends BasicBlock<?>> exceptionalSuccessors =
                    block.getExceptionalSuccessors();
            if (exceptionalSuccessors.size() > 0) {
                sb.append("\t//exceptional edges \n");
                for (Map.Entry<? extends ClassType, ? extends BasicBlock<?>> successorBlock :
                        exceptionalSuccessors.entrySet()) {
                    sb.append("\t")
                            .append(block.getTail().hashCode())
                            .append(":e -> ")
                            .append(successorBlock.getValue().getHead().hashCode())
                            .append(":n [label=\"\t")
                            .append(successorBlock.getKey().toString())
                            .append("\"color=red,ltail=\"cluster_")
                            .append(block.hashCode())
                            .append("\"]\n");
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private static String createIternalBlockStmt(Stmt stmt, boolean isStartingStmt) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t\t")
                .append(stmt.hashCode())
                .append("[label=\"")
                .append(escape(stmt.toString()))
                .append("\"");
        // mark startingstmt itself
        if (isStartingStmt || stmt.getExpectedSuccessorCount() == 0) {
            sb.append(",shape=Mdiamond,color=grey50,fillcolor=white");
        }
        sb.append("]\n");
        return sb.toString();
    }

    private static String escape(String str) {
        // ", &, <, and >
        return StringEscapeUtils.escapeXml10(str);
    }

    public static StringBuilder buildDiGraphObject(StringBuilder sb) {
        sb.append("digraph G {\n")
                .append("\tcompound=true\n")
                .append("\tlabelloc=b\n")
                .append("\tstyle=filled\n")
                .append("\tcolor=gray90\n")
                .append("\tnode [shape=box,style=filled,color=white]\n")
                .append("\tedge [fontsize=10,arrowsize=1.5,fontcolor=grey40]\n")
                .append("\tfontsize=10\n\n");
        return sb;
    }

    private static CoverageType getBlockCoverageType(@Nullable BlockDataDTO blockData) {
        if (blockData == null || blockData.coverageData == null) {
            return CoverageType.MISSING;
        }

        return switch (blockData.coverageData.coverageState) {
            case COVERED -> CoverageType.FULL;
            case NOT_COVERED -> CoverageType.NONE;
            case PARTIALLY_COVERED -> CoverageType.PARTIAL;
        };
    }

    private static String getBlockLabel(@Nullable BlockDataDTO blockData) {
        if (blockData == null || blockData.coverageData == null) {
            return "no coverage data";
        }

        int blockId = blockData.id;
        int sumHits = blockData.coverageData.lines.stream()
                .mapToInt(line -> line.hits)
                .sum();

        if (blockData.coverageData.lines.isEmpty()) {
            return String.format("Block ID: %d\\nno line coverage data", blockId);
        }

        // hits should be the same for all lines in a block
        int avgHits = sumHits / blockData.coverageData.lines.size();
        String branchesLabel = getBranchesLabel(blockData.coverageData.lines);

        return String.format("Block ID: %d\\nHits: %d\\n%s",
                blockId,
                avgHits,
                branchesLabel);
    }

    private static String getBranchesLabel(List<LineDTO> lines) {
        LineDTO branchesLine = getFirstBranchesLine(lines);
        if (branchesLine == null) {
            return "";
        }

        int coveredBranches = branchesLine.branches.covered;
        int totalBranches = branchesLine.branches.total;

        return String.format("Branches: %d/%d", coveredBranches, totalBranches);
    }

    private static LineDTO getFirstBranchesLine(List<LineDTO> lines) {
        for (LineDTO line : lines) {
            if (line.branches.total > 0) {
                return line;
            }
        }
        return null;
    }

    private static String getBlockColor(CoverageType coverageType) {
        return switch (coverageType) {
            case NONE -> "lightcoral";
            case PARTIAL -> "khaki1";
            case FULL -> "palegreen3";
            case MISSING -> "lightgrey";
        };
    }
}

