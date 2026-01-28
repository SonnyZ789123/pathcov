package com.kuleuven.blockmap.hash;

import com.kuleuven.blockmap.hash.visitor.HashStmtVisitor;
import com.kuleuven.util.codec.HashUtil;
import sootup.core.graph.BasicBlock;
import sootup.core.jimple.common.stmt.*;

import java.util.ArrayList;
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
