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

package com.kuleuven.coverage.CoverageAgent.shared;

import sootup.core.jimple.common.stmt.Stmt;

public final class StmtId {
    /**
     * Get a unique identifier inside a method for a statement based on its position and string representation.
     * Because this includes the stmt position info, it should be unique over multiple methods.
     * @param stmt The statement to identify.
     * @return A unique identifier string for the statement.
     */
    public static String getStmtId(Stmt stmt) {
        return stmt.getPositionInfo().getStmtPosition().toString() + "::" + stmt.toString();
    }

    /**
     * Extract the statement string from a statement identifier.
     * @param stmtId The statement identifier (e.g., [8:0-9]::$stack20 = <java.lang.System: java.io.PrintStream out>).
     * @return The statement string, or null if not found.
     */
    public static String getStmtStringFromId(String stmtId) {
        String[] parts = stmtId.split("::", 2);
        if (parts.length == 2) {
            return parts[1];
        }
        return null;
    }
}
