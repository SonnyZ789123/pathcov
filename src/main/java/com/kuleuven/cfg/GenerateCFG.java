package com.kuleuven.cfg;

import com.github.javaparser.quality.Nullable;
import com.kuleuven.config.AppConfig;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.util.DotExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GenerateCFG {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         *   2: outputPath             (e.g., "./out/visualization/cfg/cfg.dot")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature> [outputPath]");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];
        String outputPath = args.length >= 3 ? args[2] : null;

        try {
            Generator generator = new Generator(classPath, fullyQualifiedMethodSignature);
            ControlFlowGraph<?> cfg = generator.getCfg();

            writeOutputs(cfg, outputPath);
        } catch (IOException e) {
            System.err.println("❌ Control flow graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Writes the control flow graph's DOT representation.
     *
     * @param cfg The control flow graph to write
     * @param outputPath The optional output path
     * @throws IOException If writing fails
     */
    private static void writeOutputs(ControlFlowGraph<?> cfg,  @Nullable String outputPath) throws IOException {
        String cfgPath = outputPath != null ? outputPath : AppConfig.get("cfg.write.path");

        Path output = Path.of(cfgPath);
        Files.createDirectories(output.getParent());

        try (FileWriter writer = new FileWriter(output.toFile())) {
            String cfgAsDot = DotExporter.buildGraph(cfg, false, null, null);
            writer.write(cfgAsDot);
            System.out.println("✅ DOT file written to " + output);
        }
    }
}
