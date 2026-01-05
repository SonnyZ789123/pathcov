package com.kuleuven.icfg;

import com.kuleuven.config.AppConfig;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GenerateICFG {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature>");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];

        try {
            Generator generator = new Generator(classPath, fullyQualifiedMethodSignature);
            JimpleBasedInterproceduralCFG icfg = generator.getICfg();

            writeOutputs(generator.dotExport());
        } catch (IOException e) {
            System.err.println("❌ Control flow graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Writes the interprocedural control flow graph's DOT representation.
     *
     * @param icfgAsDot The interprocedural control flow graph to write
     * @throws IOException If writing fails
     */
    private static void writeOutputs(String icfgAsDot) throws IOException {

        String cfgPath = AppConfig.get("icfg.write.path");

        Path output = Path.of(cfgPath);
        Files.createDirectories(output.getParent());

        try (FileWriter writer = new FileWriter(output.toFile())) {
            writer.write(icfgAsDot);
            System.out.println("✅ DOT file written to " + output);
        }
    }
}
