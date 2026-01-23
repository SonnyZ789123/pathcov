package com.kuleuven.coverage.intellij.model;

import java.util.ArrayList;
import java.util.List;

public class MethodDTO {
    public String methodSignature;
    public List<LineDTO> lines = new ArrayList<>();
}
