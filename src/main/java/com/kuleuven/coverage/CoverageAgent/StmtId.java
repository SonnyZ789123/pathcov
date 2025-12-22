package com.kuleuven.coverage.CoverageAgent;

import sootup.core.jimple.common.stmt.Stmt;

public final class StmtId {
    /**
     * Get a unique identifier inside a method for a statement based on its position and string representation.
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
