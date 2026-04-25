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

package com.kuleuven.cg;

import com.github.javaparser.quality.Nullable;
import com.kuleuven.config.AppConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WriteCallGraphClasses {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         *   2: outputPath             (e.g., "./out/visualization/icfg/icfg.dot")
         *   3: projectPrefixes        (e.g., "com.kuleuven,test.features")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature> [outputPath] [projectPrefixes]");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];
        String outputPath = args.length >= 3 ? args[2] : null;
        List<String> projectPrefixes = args.length >= 4
                ? List.of(args[3].split(","))
                : null;

        try {
            Generator generator = new Generator(classPath, fullyQualifiedMethodSignature, projectPrefixes);

            ReducedCallGraph callGraph = generator.getCallGraph();

            Set<String> classNames = new HashSet<>();
            callGraph.getMethodSignatures().forEach(methodSignature -> {
                classNames.add(methodSignature.getDeclClassType().getFullyQualifiedName());
            });
            writeOutputs(classNames, outputPath);
        } catch (IOException e) {
            System.err.println("❌ Control flow graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void writeOutputs(Set<String> classNames, @Nullable String outputPath) throws IOException {
        String writeOutputPath = outputPath != null ? outputPath : AppConfig.get("cg.classes.write.path");

        Path output = Path.of(writeOutputPath);
        Files.createDirectories(output.getParent());

        String outputContent = classNames.stream().sorted().reduce((a, b) -> a + "\n" + b).orElse("");

        try (FileWriter writer = new FileWriter(output.toFile())) {
            writer.write(outputContent);
            System.out.println("✅ DOT file written to " + output);
        }
    }
}
