package com.kuleuven.coverage.CoverageAgent.shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kuleuven.config.AppConfig;
import com.kuleuven.coverage.CoverageAgent.StmtId;
import com.kuleuven.jvm.descriptor.SootMethodEncoder;
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

public final class BlockInfoByIdMap {
    private final SootMethod method;
    private final ControlFlowGraph<?> cfg;
    private final Map<Integer, BlockInfo> blocksById = new LinkedHashMap<>();
    private static final String outputPathString = AppConfig.get("coverage.block_map.write.path");

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

    public static Map<Integer, BlockInfo> readFromJson() throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<Integer, BlockInfo>>() {}.getType();

        try (InputStreamReader reader =
                     new InputStreamReader(
                             java.nio.file.Files.newInputStream(
                                     java.nio.file.Path.of(outputPathString)))) {

            return gson.fromJson(reader, type);
        }
    }

    public void dump() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        Path outputPath = Path.of(outputPathString);
        Files.createDirectories(outputPath.getParent());

        try (Writer writer = Files.newBufferedWriter(outputPath)) {
            gson.toJson(this.blocksById, writer);
        }

        System.out.println("✅ CFG block map written to " + outputPath);
    }
}
