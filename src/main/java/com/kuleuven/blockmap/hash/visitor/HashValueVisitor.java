package com.kuleuven.blockmap.hash.visitor;

import com.kuleuven.blockmap.hash.codec.ExprHashEncoder;
import com.kuleuven.blockmap.hash.codec.ImmediateHashEncoder;
import com.kuleuven.blockmap.hash.codec.RefHashEncoder;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.visitor.ValueVisitor;

import java.util.LinkedHashMap;
import java.util.Map;

public class HashValueVisitor implements ValueVisitor {

    private final StringBuilder sb = new StringBuilder();

    private final ImmediateHashEncoder imm = new ImmediateHashEncoder(sb);
    private final ExprHashEncoder expr = new ExprHashEncoder(sb, this::visitValue);
    private final RefHashEncoder ref = new RefHashEncoder(sb, this::visitValue);

    private final Map<Local, Integer> localIds = new LinkedHashMap<>();
    private int nextLocalId = 0;

    private int id(Local l) {
        return localIds.computeIfAbsent(l, k -> nextLocalId++);
    }

    public String getEncoded() {
        return sb.toString();
    }

    /* =========================================================
       🔁 THE ONLY RECURSION ENTRY POINT
       ========================================================= */
    private void visitValue(Value v) {
        v.accept(this);
    }

    /* ================= IMMEDIATE ================= */

    @Override
    public void caseLocal(@NonNull Local local) {
        int id = id(local);
        imm.encode(local, id);
    }

    @Override public void caseIntConstant(@NonNull IntConstant c)       { imm.encode(c); }
    @Override public void caseLongConstant(@NonNull LongConstant c)     { imm.encode(c); }
    @Override public void caseFloatConstant(@NonNull FloatConstant c)   { imm.encode(c); }
    @Override public void caseDoubleConstant(@NonNull DoubleConstant c) { imm.encode(c); }
    @Override public void caseStringConstant(@NonNull StringConstant c) { imm.encode(c); }
    @Override public void caseBooleanConstant(@NonNull BooleanConstant c){ imm.encode(c); }
    @Override public void caseNullConstant(@NonNull NullConstant c)     { imm.encode(c); }
    @Override public void caseClassConstant(@NonNull ClassConstant c)   { imm.encode(c); }
    @Override public void caseEnumConstant(@NonNull EnumConstant c)     { imm.encode(c); }
    @Override public void caseMethodHandle(@NonNull MethodHandle h)     { imm.encode(h); }
    @Override public void caseMethodType(@NonNull MethodType t)         { imm.encode(t); }

    @Override public void defaultCaseConstant(@NonNull Constant c) { imm.encode(c);}

    /* ================= EXPR ================= */

    @Override public void caseAddExpr(JAddExpr e) { expr.encode(e); }
    @Override public void caseSubExpr(JSubExpr e) { expr.encode(e); }
    @Override public void caseMulExpr(JMulExpr e) { expr.encode(e); }
    @Override public void caseDivExpr(JDivExpr e) { expr.encode(e); }
    @Override public void caseRemExpr(JRemExpr e) { expr.encode(e); }

    @Override public void caseAndExpr(JAndExpr e) { expr.encode(e); }
    @Override public void caseOrExpr(JOrExpr e)   { expr.encode(e); }
    @Override public void caseXorExpr(JXorExpr e) { expr.encode(e); }

    @Override public void caseShlExpr(JShlExpr e)  { expr.encode(e); }
    @Override public void caseShrExpr(JShrExpr e)  { expr.encode(e); }
    @Override public void caseUshrExpr(JUshrExpr e){ expr.encode(e); }

    @Override public void caseEqExpr(JEqExpr e) { expr.encode(e); }
    @Override public void caseNeExpr(JNeExpr e) { expr.encode(e); }
    @Override public void caseGeExpr(JGeExpr e) { expr.encode(e); }
    @Override public void caseGtExpr(JGtExpr e) { expr.encode(e); }
    @Override public void caseLeExpr(JLeExpr e) { expr.encode(e); }
    @Override public void caseLtExpr(JLtExpr e) { expr.encode(e); }

    @Override public void caseCmpExpr(JCmpExpr e)  { expr.encode(e); }
    @Override public void caseCmpgExpr(JCmpgExpr e) { expr.encode(e); }
    @Override public void caseCmplExpr(JCmplExpr e) { expr.encode(e); }

    @Override public void caseNegExpr(JNegExpr e) { expr.encode(e); }
    @Override public void caseLengthExpr(JLengthExpr e) { expr.encode(e); }
    @Override public void caseCastExpr(JCastExpr e) { expr.encode(e); }
    @Override public void caseInstanceOfExpr(JInstanceOfExpr e) { expr.encode(e); }

    @Override public void caseNewExpr(JNewExpr e) { expr.encode(e); }
    @Override public void caseNewArrayExpr(JNewArrayExpr e) { expr.encode(e); }
    @Override public void caseNewMultiArrayExpr(JNewMultiArrayExpr e) { expr.encode(e); }

    @Override public void caseVirtualInvokeExpr(JVirtualInvokeExpr e) { expr.encode(e); }
    @Override public void caseStaticInvokeExpr(JStaticInvokeExpr e) { expr.encode(e); }
    @Override public void caseSpecialInvokeExpr(JSpecialInvokeExpr e) { expr.encode(e); }
    @Override public void caseInterfaceInvokeExpr(JInterfaceInvokeExpr e) { expr.encode(e); }
    @Override public void caseDynamicInvokeExpr(JDynamicInvokeExpr e) { expr.encode(e); }

    @Override public void casePhiExpr(JPhiExpr e) { expr.encode(e); }

    @Override
    public void defaultCaseExpr(Expr exprNode) {
        expr.encodeFallback(exprNode);
    }

    /* ================= REF ================= */

    @Override public void caseStaticFieldRef(JStaticFieldRef r) { ref.encode(r); }
    @Override public void caseInstanceFieldRef(JInstanceFieldRef r) { ref.encode(r); }
    @Override public void caseArrayRef(JArrayRef r) { ref.encode(r); }
    @Override public void caseParameterRef(JParameterRef r) { ref.encode(r); }
    @Override public void caseCaughtExceptionRef(JCaughtExceptionRef r) { ref.encode(r); }
    @Override public void caseThisRef(JThisRef r) { ref.encode(r); }

    @Override
    public void defaultCaseRef(Ref refNode) {
        ref.encodeFallback(refNode);
    }

    /* ================= GLOBAL FALLBACK ================= */

    @Override
    public void defaultCaseValue(Value v) {
        sb.append("VAL(").append(v.getClass().getSimpleName()).append(")");
    }
}
