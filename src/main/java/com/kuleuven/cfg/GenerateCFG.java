package com.kuleuven.cfg;

import sootup.core.graph.ControlFlowGraph;
import sootup.core.util.DotExporter;

import java.io.FileWriter;
import java.io.IOException;

public class GenerateCFG {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature> ");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];

        try {
            Generator generator = new Generator(classPath, fullyQualifiedMethodSignature);
            ControlFlowGraph<?> cfg = generator.getCfg();

            writeOutputs(cfg);
        } catch (IOException e) {
            System.err.println("❌ Control flow graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Writes the control flow graph's DOT representation.
     *
     * @param cfg The control flow graph to write
     * @throws IOException If writing fails
     */
    private static void writeOutputs(ControlFlowGraph<?> cfg) throws IOException {
        // Ensure output directory exists
        (new java.io.File("out")).mkdirs();

        // Write DOT graph representation
        String filename = "out/cfg.dot";
        try (FileWriter writer = new FileWriter(filename)) {
            String cfgAsDot = DotExporter.buildGraph(cfg, false, null, null);
            writer.write(cfgAsDot);
            System.out.println("✅ DOT file written to " + filename);
        }
    }
}
