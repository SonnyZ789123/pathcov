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

package com.kuleuven.cfg.CoverageAgent.shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kuleuven.config.AppConfig;
import com.kuleuven.coverage.CoverageAgent.shared.StmtId;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import com.kuleuven.jvm.descriptor.SootMethodEncoder;
import org.jspecify.annotations.Nullable;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BlockInfo to ID mapping for a single CFG.
 * @deprecated Conversion to interprocedural CFGs.
 */
@Deprecated
public final class BlockInfoByIdMap {
    private final SootMethod method;
    private final ControlFlowGraph<?> cfg;
    private final Map<Integer, BlockInfo> blocksById = new LinkedHashMap<>();
    private static final String outputPathString = AppConfig.get("cfg.block_map.write.path");

    public BlockInfoByIdMap(SootMethod method, ControlFlowGraph<?> cfg) {
        this.method = method;
        this.cfg = cfg;
        init();
    }

    private void init() {
        int nextId = 0;
        for (BasicBlock<?> block : cfg.getBlocks()) {
            Stmt entry = block.getHead();

            int lineNumber = entry.getPositionInfo().getStmtPosition().getFirstLine();

            BlockInfo info = new BlockInfo(
                    nextId,
                    method.getDeclaringClassType().getFullyQualifiedName(),
                    method.getName(),
                    SootMethodEncoder.toJvmMethodDescriptor(method),
                    StmtId.getStmtId(entry),
                    lineNumber
            );

            blocksById.put(nextId++, info);
        }
    }

    public Map<Integer, BlockInfo> getBlocksById() {
        return blocksById;
    }

    public static Map<Integer, BlockInfo> readFromJson(@Nullable String optionalBlockMapPath) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<Integer, BlockInfo>>() {}.getType();

        String blockMapPath = optionalBlockMapPath != null ? optionalBlockMapPath : outputPathString;

        try (InputStreamReader reader =
                     new InputStreamReader(
                             Files.newInputStream(
                                     Path.of(blockMapPath)))) {

            return gson.fromJson(reader, type);
        }
    }

    public void dump(@Nullable String optionalOutputPathString) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        Path outputPath = Path.of(optionalOutputPathString != null ? optionalOutputPathString : outputPathString);
        Files.createDirectories(outputPath.getParent());

        try (Writer writer = Files.newBufferedWriter(outputPath)) {
            gson.toJson(this.blocksById, writer);
        }

        System.out.println("✅ CFG block map written to " + outputPath);
    }
}
