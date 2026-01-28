package com.kuleuven.blockmap.hash.visitor;

import com.kuleuven.util.codec.HashUtil;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.*;
import sootup.core.jimple.visitor.StmtVisitor;

public class HashStmtVisitor implements StmtVisitor {

    private final StringBuilder sb = new StringBuilder();

    private void encodeValue(Value v) {
        HashValueVisitor valueHasher = new HashValueVisitor(); // fresh per value to avoid carry-over
        v.accept(valueHasher);
        sb.append(valueHasher.getEncoded());
    }

    public String getHash() {
        String stmtStructure = sb.toString();

        return HashUtil.sha256(stmtStructure);
    }

    /* ================= ASSIGN ================= */

    @Override
    public void caseAssignStmt(JAssignStmt stmt) {
        sb.append("ASSIGN(");

        sb.append("L=");
        encodeValue(stmt.getLeftOp());

        sb.append(",R=");
        encodeValue(stmt.getRightOp());

        sb.append(")");
    }

    /* ================= INVOKE ================= */

    @Override
    public void caseInvokeStmt(JInvokeStmt stmt) {
        sb.append("INVOKE(");
        stmt.getInvokeExpr().ifPresent(this::encodeValue);
        sb.append(")");
    }

    /* ================= IF ================= */

    @Override
    public void caseIfStmt(JIfStmt stmt) {
        sb.append("IF(");
        encodeValue(stmt.getCondition());
        sb.append(",TARGETS=").append(stmt.getExpectedSuccessorCount());
        sb.append(")");
    }

    /* ================= SWITCH ================= */

    @Override
    public void caseSwitchStmt(JSwitchStmt stmt) {
        sb.append("SWITCH(");

        // Key expression
        sb.append("KEY=");
        encodeValue(stmt.getKey());

        // Switch type
        if (stmt.isTableSwitch()) {
            sb.append(",TABLE");
        } else {
            sb.append(",LOOKUP");
        }

        // Case values (order matters!)
        sb.append(",CASES=[");
        for (int i = 0; i < stmt.getValues().size(); i++) {
            sb.append(stmt.getValues().get(i).getValue());
            if (i < stmt.getValues().size() - 1) sb.append(",");
        }
        sb.append("]");

        // Total branch count (includes default)
        sb.append(",TARGETS=").append(stmt.getExpectedSuccessorCount());

        sb.append(")");
    }


    /* ================= RETURN ================= */

    @Override
    public void caseReturnStmt(JReturnStmt stmt) {
        sb.append("RETURN(");
        encodeValue(stmt.getOp());
        sb.append(")");
    }

    @Override
    public void caseReturnVoidStmt(JReturnVoidStmt stmt) {
        sb.append("RETURN_VOID");
    }

    /* ================= THROW ================= */

    @Override
    public void caseThrowStmt(JThrowStmt stmt) {
        sb.append("THROW(");
        encodeValue(stmt.getOp());
        sb.append(")");
    }

    /* ================= IDENTITY ================= */

    @Override
    public void caseIdentityStmt(JIdentityStmt stmt) {
        sb.append("IDENTITY(");
        encodeValue(stmt.getLeftOp());
        sb.append("<-");
        encodeValue(stmt.getRightOp());
        sb.append(")");
    }

    /* ================= MONITOR ================= */

    @Override
    public void caseEnterMonitorStmt(JEnterMonitorStmt stmt) {
        sb.append("ENTER_MONITOR(");
        encodeValue(stmt.getOp());
        sb.append(")");
    }

    @Override
    public void caseExitMonitorStmt(JExitMonitorStmt stmt) {
        sb.append("EXIT_MONITOR(");
        encodeValue(stmt.getOp());
        sb.append(")");
    }

    /* ================= CONTROL FLOW ================= */

    @Override public void caseGotoStmt(JGotoStmt stmt) {
        sb.append("GOTO");
    }

    @Override
    public void caseRetStmt(JRetStmt stmt) {
        sb.append("RET(");
        encodeValue(stmt.getStmtAddress());
        sb.append(")");
    }

    /* ================= MISC ================= */

    @Override public void caseBreakpointStmt(JBreakpointStmt stmt) { sb.append("BREAKPOINT"); }
    @Override public void caseNopStmt(JNopStmt stmt) { sb.append("NOP"); }

    /* ================= FALLBACK ================= */

    @Override
    public void defaultCaseStmt(Stmt stmt) {
        sb.append(stmt.getClass().getSimpleName());
    }
}
