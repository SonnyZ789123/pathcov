package com.kuleuven.jdart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.config.AppConfig;
import com.kuleuven.coverage.CoverageAgent.shared.Out;
import org.jspecify.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateJDartInstructionCoverage {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: coveragePathsOutputPath              (e.g., "./out/coverage.json")
         *   1: outputPath                           (e.g., "./out/jdart_instruction_paths.json")
         */
        if (args.length < 1) {
            System.out.println("Expects args <coveragePathsOutputPath> [outputPath]");
            System.exit(1);
        }

        String coveragePathsOutputPath = args[0];
        String outputPath = args.length >= 2 ? args[1] : null;

        try {
            Out out = new Out(coveragePathsOutputPath);

            Map<String, List<int[]>> instructionPathsByMethod = new HashMap<>();
            // ASM method full name is something like: com.kuleuven._examples.Foo.bar(I)I
            for (String methodFullName : out.getMethodFullNames()) {
                instructionPathsByMethod.put(methodFullName, out.getInstructionPaths(methodFullName));
            }
            writeOutputs(instructionPathsByMethod, outputPath);
        } catch (IOException e) {
                System.err.println("❌ Failed to read block coverage map from path " + coveragePathsOutputPath);
                System.exit(1);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Failed to deserialize block coverage map: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void writeOutputs(
            Map<String, List<int[]>> instructionPathsByMethod,
            @Nullable String outputPathString
    ) throws IOException {
        Path outputPath;
        if (outputPathString != null) {
            outputPath = Path.of(outputPathString);
        } else {
            outputPath = Path.of(AppConfig.get("execution_paths.jdart.write.path"));
        }

        Files.createDirectories(outputPath.getParent());
        Gson gson = new GsonBuilder().create();

        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write(gson.toJson(instructionPathsByMethod));
            System.out.println("✅ Instruction paths written to " + outputPath);
        }
    }
}
