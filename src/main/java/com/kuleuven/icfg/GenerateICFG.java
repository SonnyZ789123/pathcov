package com.kuleuven.icfg;

import com.github.javaparser.quality.Nullable;
import com.kuleuven.config.AppConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GenerateICFG {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         *   2: outputPath             (e.g., "./out/visualization/icfg/icfg.dot")
         *   3: projectPrefixes        (e.g., "com.kuleuven,test.features")
         */
        if (args.length < 2) {
            System.out.println("Expects args <classPath> <fullyQualifiedMethodSignature> [outputPath] [projectPrefixes]");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];
        String outputPath = args.length >= 3 ? args[2] : null;
        List<String> projectPrefixes = args.length >= 4
                ? List.of(args[3].split(","))
                : null;

        try {
            Generator generator = new Generator(classPath, fullyQualifiedMethodSignature, projectPrefixes);

            writeOutputs(generator.dotExport(), outputPath);
        } catch (IOException e) {
            System.err.println("❌ Control flow graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Writes the interprocedural control flow graph's DOT representation.
     *
     * @param icfgAsDot The interprocedural control flow graph to write
     * @param outputPath The optional output path
     * @throws IOException If writing fails
     */
    private static void writeOutputs(String icfgAsDot, @Nullable String outputPath) throws IOException {
        String writeOutputPath = outputPath != null ? outputPath : AppConfig.get("icfg.write.path");

        Path output = Path.of(writeOutputPath);
        Files.createDirectories(output.getParent());

        try (FileWriter writer = new FileWriter(output.toFile())) {
            writer.write(icfgAsDot);
            System.out.println("✅ DOT file written to " + output);
        }
    }
}
