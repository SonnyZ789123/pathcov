package com.kuleuven.coverage.intellij.model;

import java.util.ArrayList;
import java.util.List;

public class SwitchDTO {
    public int index;
    public OutcomeDTO defaultBranch;
    public List<CaseDTO> cases = new ArrayList<>();
}
