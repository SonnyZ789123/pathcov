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

package com.kuleuven.coverage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.blockmap.coverage.BranchCoverage;
import com.kuleuven.blockmap.model.BlockMapDTO;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class GenerateBranchCoverage {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: blockMapPath           (e.g., "./out/icfg_block_map.json")
         */
        if (args.length < 1) {
            System.out.println("Expects args <blockMapPath>");
            System.exit(1);
        }
        String blockMapPath = args[0];

        BlockMapDTO blockMap = null;
        try (FileReader reader = new FileReader(Path.of(blockMapPath).toFile())) {
            Gson gson = new GsonBuilder().create();
            blockMap = gson.fromJson(reader, BlockMapDTO.class);
        } catch (IOException e) {
            System.err.println("❌ Failed to read block map from path " + blockMapPath);
            System.exit(1);
        }


        double branchCoverage = BranchCoverage.calculate(blockMap);
        System.out.println("✅ Branch coverage: " + String.format("%.2f", branchCoverage) + "%");
    }
}
