package com.kuleuven.blockmap.hash;

import com.kuleuven.blockmap.hash.visitor.HashStmtVisitor;
import com.kuleuven.util.codec.HashUtil;
import sootup.core.graph.BasicBlock;
import sootup.core.jimple.common.stmt.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockHashBuilder {

    private final BasicBlock<?> block;

    public BlockHashBuilder(BasicBlock<?> block) {
        this.block = block;
    }

    public String build() {
        List<String> stmtHashes = new ArrayList<>();

        for (Stmt stmt : block.getStmts()) {
            stmtHashes.add(hashStmt(stmt));
        }

        StringBuilder blockStructure = new StringBuilder();
        blockStructure.append("STMTS=").append(stmtHashes);
        blockStructure.append("|SUCC=").append(block.getSuccessors().size());
        blockStructure.append("|EXC_SUCC=").append(
                block.getExceptionalSuccessors().keySet().stream()
                        .map(Object::toString)
                        .sorted()
                        .toList()
        );

        // Include predecessor edge context to disambiguate structurally identical blocks
        // at different positions in the CFG (e.g., two `if` blocks with the same condition pattern).
        // Encodes "I am the Nth successor of a predecessor with stmt-list hash X".
        // Uses ALL predecessor stmts (not just tail) to differentiate predecessors that
        // have identical tail statements but different block sizes/contents.
        // This is position-independent (no line numbers) but structurally unique.
        List<String> predEdges = new ArrayList<>();
        for (BasicBlock<?> pred : block.getPredecessors()) {
            int branchIdx = pred.getSuccessors().indexOf(block);
            List<String> predStmtHashes = new ArrayList<>();
            for (Stmt predStmt : pred.getStmts()) {
                predStmtHashes.add(hashStmt(predStmt));
            }
            predEdges.add(predStmtHashes + "@" + branchIdx);
        }
        Collections.sort(predEdges);
        blockStructure.append("|PRED_EDGE=").append(predEdges);

        return HashUtil.sha256(blockStructure.toString());
    }


    /*
     Look at the Stmt class of SootUp v2.0.1 to see all the different statement types.
     We should cover all the statement types.
     */
    private String hashStmt(Stmt stmt) {
        HashStmtVisitor visitor = new HashStmtVisitor();
        stmt.accept(visitor);
        return visitor.getHash();
    }

}
