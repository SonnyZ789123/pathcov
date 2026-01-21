package com.kuleuven.coverage.intellij.model;

import java.util.ArrayList;
import java.util.List;

public class LineDTO {
    public int line;
    public int hits;

    public InstructionSummaryDTO instructions;
    public BranchSummaryDTO branches;

    public List<JumpDTO> jumps = new ArrayList<>();
    public List<SwitchDTO> switches = new ArrayList<>();
}

