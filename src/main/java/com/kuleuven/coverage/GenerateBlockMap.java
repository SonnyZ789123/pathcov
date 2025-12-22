package com.kuleuven.coverage;

import com.kuleuven.cfg.Generator;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfoByIdMap;
import sootup.core.graph.MutableBlockControlFlowGraph;

import java.io.IOException;

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

        BlockInfoByIdMap blockMap = createCfgBlockMap(classPath, fullyQualifiedMethodSignature);

        try {
            blockMap.dump();
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

        MutableBlockControlFlowGraph cfg = (MutableBlockControlFlowGraph) generator.getCfg();

        return new BlockInfoByIdMap(generator.method, cfg);
    }
}
