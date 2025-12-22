package com.kuleuven.coverage.CoverageAgent.shared;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

public final class Out {
    public static List<int[]> get(String outputPath) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(outputPath));

        @SuppressWarnings("unchecked")
        List<int[]> executionPaths = (List<int[]>) ois.readObject();

        return executionPaths;
    }
}
