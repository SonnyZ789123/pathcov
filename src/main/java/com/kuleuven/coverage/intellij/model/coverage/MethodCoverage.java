package com.kuleuven.coverage.intellij.model.coverage;

import java.util.ArrayList;
import java.util.List;

public class MethodCoverage {
    public String methodSignature;
    public List<LineCoverage> lines = new ArrayList<>();
}
