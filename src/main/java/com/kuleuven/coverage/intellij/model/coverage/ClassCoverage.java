package com.kuleuven.coverage.intellij.model.coverage;

import java.util.ArrayList;
import java.util.List;

public class ClassCoverage {
    public String name;
    public List<MethodCoverage> methods = new ArrayList<>();
}
