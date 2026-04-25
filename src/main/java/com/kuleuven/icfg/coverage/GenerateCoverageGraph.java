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

package com.kuleuven.icfg.coverage;

import com.github.javaparser.quality.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.blockmap.model.BlockMapDTO;
import com.kuleuven.config.AppConfig;
import com.kuleuven.icfg.Generator;
import com.kuleuven.icfg.sootup.analysis.interprocedural.icfg.BuildICFGGraph;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GenerateCoverageGraph {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         *   2: blockMapPath           (e.g., "./out/icfg_block_map.json")
         *   3: outputPath             (e.g., "/data/coverage_graph.dot")
         *   4: projectPrefixes        (e.g., "com.kuleuven,test.SimpleExample")
         */
        if (args.length < 3) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature> <blockMapPath> [outputPath] [projectPrefixes]");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];
        String blockMapPath = args[2];
        String outputPath = args.length >= 4 ? args[3] : null;
        List<String> projectPrefixes = args.length >= 5
                ? List.of(args[4].split(","))
                : null;

        BlockMapDTO blockMap = null;
        try (FileReader reader = new FileReader(Path.of(blockMapPath).toFile())) {
            Gson gson = new GsonBuilder().create();
            blockMap = gson.fromJson(reader, BlockMapDTO.class);
        } catch (IOException e) {
            System.err.println("❌ Failed to read block map from path " + blockMapPath);
            System.exit(1);
        }

        try {
            Generator generator = new Generator(classPath, fullyQualifiedMethodSignature, projectPrefixes);
            JimpleBasedInterproceduralCFG icfg = generator.getICfg();

            BuildICFGGraph builder = new BuildICFGGraph(generator.getView(), icfg, new BlockCoverageMap(blockMap));
            String coverageGraphAsDot = builder.buildICFGGraph(true);

            writeOutputs(coverageGraphAsDot, outputPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to write coverage graph: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void writeOutputs(String coverageGraphAsDot, @Nullable String outputPath) throws IOException {
        String writeOutputPath = outputPath != null ? outputPath : AppConfig.get("coverage.icfg.write.path");

        Path output = Path.of(writeOutputPath);
        Files.createDirectories(output.getParent());

        try (FileWriter writer = new FileWriter(output.toFile())) {
            writer.write(coverageGraphAsDot);
            System.out.println("✅ ICFG coverage DOT file written to " + output);
        }
    }
}
