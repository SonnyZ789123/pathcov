package com.kuleuven.coverage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.cfg.Generator;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfoByIdMap;
import sootup.core.graph.MutableBlockControlFlowGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class GenerateBlockMap {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven._examples.Foo: int foo(int)>")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature>");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];

        Map<Integer, BlockInfo> blocksById = createCfgBlockMap(classPath, fullyQualifiedMethodSignature);

        Path outputPath = Path.of("out/cfg_block_map.json");
        try {
            writeCfgBlockMap(blocksById, outputPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to write CFG block mapping to " + outputPath);
            System.exit(1);
        }
    }

    private static Map<Integer, BlockInfo> createCfgBlockMap(String classPath, String fullyQualifiedMethodSignature) {
        Generator generator = new Generator(
                classPath,
                fullyQualifiedMethodSignature
        );

        MutableBlockControlFlowGraph cfg = (MutableBlockControlFlowGraph) generator.getCfg();

        BlockInfoByIdMap blockInfoByIdMap = new BlockInfoByIdMap(generator.method, cfg);
        return blockInfoByIdMap.getBlocksById();
    }

    private static void writeCfgBlockMap(Map<Integer, BlockInfo> blocksById, Path outputPath) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        Files.createDirectories(outputPath.getParent());

        try (Writer writer = Files.newBufferedWriter(outputPath)) {
            gson.toJson(blocksById, writer);
        }

        System.out.println("✅ CFG block mapping written to " + outputPath);
    }

}
