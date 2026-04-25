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
