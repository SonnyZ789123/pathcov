package com.kuleuven.blockmap.hash.codec;

import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.*;

public final class ImmediateHashEncoder {

    private final StringBuilder sb;

    public ImmediateHashEncoder(StringBuilder sb) {
        this.sb = sb;
    }

    /* ================= LOCALS ================= */

    public void encode(Local local, int id) {
        sb.append("LOCAL#").append(id)
                .append("(").append(local.getType()).append(")");
    }

    /* ================= CONSTANTS ================= */

    public void encode(IntConstant c) {
        sb.append("INT(").append(c.getValue()).append(")");
    }

    public void encode(LongConstant c) {
        sb.append("LONG(").append(c.getValue()).append(")");
    }

    public void encode(FloatConstant c) {
        sb.append("FLOAT(").append(c.getValue()).append(")");
    }

    public void encode(DoubleConstant c) {
        sb.append("DOUBLE(").append(c.getValue()).append(")");
    }

    public void encode(StringConstant c) {
        sb.append("STRING(\"").append(c.getValue()).append("\")");
    }

    public void encode(BooleanConstant c) {
        sb.append("BOOL(").append(c.toString()).append(")");
    }

    public void encode(NullConstant c) {
        sb.append("NULL");
    }

    public void encode(ClassConstant c) {
        sb.append("CLASS(").append(c.getValue()).append(")");
    }

    public void encode(EnumConstant c) {
        sb.append("ENUM(").append(c.toString()).append(")");
    }

    public void encode(MethodHandle handle) {
        sb.append("METHOD_HANDLE(")
                .append(handle.getKind().getValueName())
                .append(",")
                .append(handle.getReferenceSignature())
                .append(")");
    }

    public void encode(MethodType methodType) {
        sb.append("METHOD_TYPE(")
                .append(methodType.getReturnType())
                .append(methodType.getParameterTypes())
                .append(")");
    }

    public void encode(Constant c) {
        sb.append("CONST(").append(c.getClass().getSimpleName()).append(")");
    }
}
