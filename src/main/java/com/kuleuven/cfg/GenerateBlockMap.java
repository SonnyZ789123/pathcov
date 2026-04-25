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

package com.kuleuven.cfg;

import com.kuleuven.icfg.CoverageAgent.shared.BlockInfoByIdMap;

import java.io.IOException;
import java.util.List;

/**
 * Generate the block map for a single CFG.
 * @deprecated Not maintained anymore. Use the ICFG module instead.
 */
@Deprecated
public class GenerateBlockMap {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven._examples.Foo: int foo(int)>")
         *   2: outputPath            (e.g., "./output/block_map.json")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature> [outputPath]");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];
        String outputPath = args.length >= 3 ? args[2] : null;

        BlockInfoByIdMap blockMap = createCfgBlockMap(classPath, fullyQualifiedMethodSignature);

        try {
            blockMap.dump(outputPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to write CFG block map: " + e.getMessage());
            System.exit(1);
        }
    }

    private static BlockInfoByIdMap createCfgBlockMap(String classPath, String fullyQualifiedMethodSignature) {
        Generator generator = new Generator(
                classPath,
                fullyQualifiedMethodSignature
        );

        return new BlockInfoByIdMap(List.of(generator.method));
    }
}
