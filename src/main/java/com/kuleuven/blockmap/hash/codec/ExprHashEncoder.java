package com.kuleuven.blockmap.hash.codec;

import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.*;

import java.util.function.Consumer;

public final class ExprHashEncoder {

    private final StringBuilder sb;
    private final Consumer<Value> visitValue;

    public ExprHashEncoder(StringBuilder sb, Consumer<Value> visitValue) {
        this.sb = sb;
        this.visitValue = visitValue;
    }

    private void visit(Value v) {
        visitValue.accept(v);
    }

    /* ================= BINOPS ================= */

    private void binop(String op, AbstractBinopExpr e) {
        sb.append(op).append("(");
        visit(e.getOp1());
        sb.append(",");
        visit(e.getOp2());
        sb.append(")");
    }

    public void encode(JAddExpr e) { binop("ADD", e); }
    public void encode(JSubExpr e) { binop("SUB", e); }
    public void encode(JMulExpr e) { binop("MUL", e); }
    public void encode(JDivExpr e) { binop("DIV", e); }
    public void encode(JRemExpr e) { binop("REM", e); }

    public void encode(JAndExpr e) { binop("AND", e); }
    public void encode(JOrExpr e)  { binop("OR", e); }
    public void encode(JXorExpr e) { binop("XOR", e); }

    public void encode(JShlExpr e)  { binop("SHL", e); }
    public void encode(JShrExpr e)  { binop("SHR", e); }
    public void encode(JUshrExpr e) { binop("USHR", e); }

    public void encode(JEqExpr e) { binop("EQ", e); }
    public void encode(JNeExpr e) { binop("NE", e); }
    public void encode(JGeExpr e) { binop("GE", e); }
    public void encode(JGtExpr e) { binop("GT", e); }
    public void encode(JLeExpr e) { binop("LE", e); }
    public void encode(JLtExpr e) { binop("LT", e); }

    public void encode(JCmpExpr e)  { binop("CMP", e); }
    public void encode(JCmpgExpr e) { binop("CMPG", e); }
    public void encode(JCmplExpr e) { binop("CMPL", e); }

    /* ================= UNARY ================= */

    public void encode(JNegExpr e) {
        sb.append("NEG(");
        visit(e.getOp());
        sb.append(")");
    }

    public void encode(JLengthExpr e) {
        sb.append("LEN(");
        visit(e.getOp());
        sb.append(")");
    }

    /* ================= TYPE OPS ================= */

    public void encode(JCastExpr e) {
        sb.append("CAST(").append(e.getType()).append(",");
        visit(e.getOp());
        sb.append(")");
    }

    public void encode(JInstanceOfExpr e) {
        sb.append("INSTANCEOF(").append(e.getCheckType()).append(",");
        visit(e.getOp());
        sb.append(")");
    }

    /* ================= ALLOCATION ================= */

    public void encode(JNewExpr e) {
        sb.append("NEW(").append(e.getType()).append(")");
    }

    public void encode(JNewArrayExpr e) {
        sb.append("NEWARRAY(").append(e.getBaseType()).append(",");
        visit(e.getSize());
        sb.append(")");
    }

    public void encode(JNewMultiArrayExpr e) {
        sb.append("NEWMULTIARRAY(").append(e.getBaseType());
        e.getSizes().forEach(size -> {
            sb.append(",");
            visit(size);
        });
        sb.append(")");
    }

    /* ================= INVOKES ================= */

    private void encodeInvoke(String kind, AbstractInvokeExpr e) {
        sb.append(kind).append("CALL(").append(e.getMethodSignature());
        e.getArgs().forEach(arg -> {
            sb.append(",");
            visit(arg);
        });
        sb.append(")");
    }

    public void encode(JVirtualInvokeExpr e)   { encodeInvoke("VIRTUAL", e); }
    public void encode(JStaticInvokeExpr e)    { encodeInvoke("STATIC", e); }
    public void encode(JSpecialInvokeExpr e)   { encodeInvoke("SPECIAL", e); }
    public void encode(JInterfaceInvokeExpr e) { encodeInvoke("INTERFACE", e); }
    public void encode(JDynamicInvokeExpr e)   { encodeInvoke("DYNAMIC", e); }

    /* ================= SSA ================= */

    public void encode(JPhiExpr v) {
        sb.append("PHI(");
        v.getArgs().forEach(arg -> {
            visit(arg);
            sb.append(",");
        });
        sb.append(")");
    }

    /* ================= FALLBACK ================= */

    public void encodeFallback(Expr expr) {
        sb.append(expr.getClass().getSimpleName());
    }
}
