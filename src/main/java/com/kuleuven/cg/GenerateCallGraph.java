package com.kuleuven.cg;

import com.github.javaparser.quality.Nullable;
import com.kuleuven.config.AppConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateCallGraph {
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
            String cgAsDot = generator.getCallGraph().exportAsDot().collect(Collectors.joining("\n"));

            writeOutputs(cgAsDot, outputPath);
        } catch (IOException e) {
            System.err.println("❌ Call graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void writeOutputs(String icfgAsDot, @Nullable String outputPath) throws IOException {
        String writeOutputPath = outputPath != null ? outputPath : AppConfig.get("cg.write.path");

        Path output = Path.of(writeOutputPath);
        Files.createDirectories(output.getParent());

        try (FileWriter writer = new FileWriter(output.toFile())) {
            writer.write(icfgAsDot);
            System.out.println("✅ DOT file written to " + output);
        }
    }
}
