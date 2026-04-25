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

package com.kuleuven.jimple;

import com.github.javaparser.quality.Nullable;
import com.kuleuven.config.AppConfig;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GenerateJimpleClass {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified class name (e.g., "com.kuleuven.Library")
         *   2: outputDirPath             (e.g., "./out/")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <fullyQualifiedClassName> [outputDirPath]");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedClassName = args[1];
        String outputDirPath = args.length >= 3 ? args[2] : null;

        try {
            // Load classes from the given classpath
            AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath);
            JavaView view = new JavaView(inputLocation);

            SootClass sootClass = view.getClass(
                            view.getIdentifierFactory().getClassType(fullyQualifiedClassName))
                    .orElseThrow(() -> new IllegalArgumentException("Class " + fullyQualifiedClassName + " not found"));

            writeOutputs(sootClass, outputDirPath);
        } catch (IOException e) {
            System.err.println("❌ Jimple class generation failed: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ " + e.getMessage());
            System.exit(1);
        }
    }

    private static void writeOutputs(SootClass sootClass, @Nullable String outputDirPath) throws IOException {
        String writeOutputDirPath = outputDirPath != null ? outputDirPath : AppConfig.get("jimple.class.write.path");

        // Get the simple class name
        String name = sootClass.getName();
        int lastDot = name.lastIndexOf('.');
        int lastDollar = name.lastIndexOf('$');
        int idx = Math.max(lastDot, lastDollar);
        String simpleName = name.substring(idx + 1);

        String writeOutputPath = writeOutputDirPath + File.separator + simpleName + ".jimple";

        Path output = Path.of(writeOutputPath);
        Files.createDirectories(output.getParent());

        try (FileWriter writer = new FileWriter(output.toFile())) {
            writer.write(sootClass.print());
            System.out.println("✅ Jimple file written to " + output);
        }
    }
}
