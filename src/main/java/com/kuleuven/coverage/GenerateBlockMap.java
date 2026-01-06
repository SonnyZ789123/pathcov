package com.kuleuven.coverage;

import com.kuleuven.cfg.Generator;
import com.kuleuven.icfg.CoverageAgent.shared.BlockInfoByIdMap;

import java.io.IOException;
import java.util.List;

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
