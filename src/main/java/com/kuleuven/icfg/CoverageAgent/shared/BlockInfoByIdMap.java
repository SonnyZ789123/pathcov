package com.kuleuven.icfg.CoverageAgent.shared;

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
import java.util.List;
import java.util.Map;

public final class BlockInfoByIdMap {
    private final List<SootMethod> methods;
    private final Map<Integer, BlockInfo> blocksById = new LinkedHashMap<>();
    private static final String outputPathString = AppConfig.get("icfg.block_map.write.path");

    public BlockInfoByIdMap(List<SootMethod> methods) {
        this.methods = methods;
        init();
    }

    private void init() {
        int nextId = 0;
        for (SootMethod method : methods) {
            ControlFlowGraph<?> cfg = method.getBody().getControlFlowGraph();

            for (BasicBlock<?> block : cfg.getBlocks()) {
                Stmt entry = block.getHead();

                int lineNumber = entry.getPositionInfo().getStmtPosition().getFirstLine();

                // The block id should be unique across all methods.
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
                             java.nio.file.Files.newInputStream(
                                     java.nio.file.Path.of(blockMapPath)))) {

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

        System.out.println("✅ ICFG block map written to " + outputPath);
    }
}

