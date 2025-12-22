package com.kuleuven.coverage.graph;

/*
Logic referenced of CfgCreator of SootUp 2.0.
*/
public enum CoverageEdgeType {
    NORMAL,
    TRUE_BRANCH,
    FALSE_BRANCH,
    SWITCH_BRANCH,
    GOTO_BRANCH
}
