package com.kuleuven.blockmap.hash.codec;

import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.ref.*;

import java.util.function.Consumer;

public final class RefHashEncoder {

    private final StringBuilder sb;
    private final Consumer<Value> visitValue; // recursion entry point

    public RefHashEncoder(StringBuilder sb, Consumer<Value> visitValue) {
        this.sb = sb;
        this.visitValue = visitValue;
    }

    private void visit(Value v) {
        visitValue.accept(v);   // 🔁 recurse via full ValueVisitor
    }

    /* ================= FIELD REFS ================= */

    public void encode(JStaticFieldRef ref) {
        sb.append("STATIC_FIELD(")
                .append(ref.getFieldSignature())
                .append(")");
    }

    public void encode(JInstanceFieldRef ref) {
        sb.append("INSTANCE_FIELD(")
                .append(ref.getFieldSignature())
                .append(",BASE=");
        visit(ref.getBase());          // base object expression
        sb.append(")");
    }

    /* ================= ARRAY REF ================= */

    public void encode(JArrayRef ref) {
        sb.append("ARRAY_REF(BASE=");
        visit(ref.getBase());
        sb.append(",INDEX=");
        visit(ref.getIndex());
        sb.append(")");
    }

    /* ================= SPECIAL JVM REFS ================= */

    public void encode(JParameterRef ref) {
        sb.append("PARAM(")
                .append(ref.getIndex())
                .append(":")
                .append(ref.getType())
                .append(")");
    }

    public void encode(JCaughtExceptionRef ref) {
        sb.append("CAUGHT_EXCEPTION(")
                .append(ref.getType())
                .append(")");
    }

    public void encode(JThisRef ref) {
        sb.append("THIS(")
                .append(ref.getType())
                .append(")");
    }

    /* ================= FALLBACK ================= */

    public void encodeFallback(Ref ref) {
        sb.append(ref.getClass().getSimpleName());
    }
}
