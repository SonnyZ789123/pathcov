package com.kuleuven.jdart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.config.AppConfig;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import com.kuleuven.coverage.CoverageAgent.shared.BlockInfoByIdMap;
import com.kuleuven.coverage.CoverageAgent.shared.Out;
import com.kuleuven.jvm.descriptor.SootMethodEncoder;
import org.jspecify.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kuleuven.coverage.CoverageAgent.util.BlockInfoUtil.extractMethodSignatures;

public class GenerateJDartInstructionCoverage {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: coveragePathsOutputPath              (e.g., "./out/coverage.out")
         *   1: blockMapPath                         (e.g., "./out/cfg_block_map.json")
         *   2: outputPath                           (e.g., "./out/jdart_instruction_paths.json")
         */
        if (args.length < 2) {
            System.out.println("Expects args <coveragePathsOutputPath> <blockMapPath> [outputPath]");
            System.exit(1);
        }

        String coveragePathsOutputPath = args[0];
        String blockMapPath = args[1];
        String outputPath = args.length >= 3 ? args[2] : null;

        Map<Integer, BlockInfo> blockMap = null;
        try {
            blockMap = BlockInfoByIdMap.readFromJson(blockMapPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to load block map: " + e.getMessage());
            System.exit(1);
        }

        try {
            Out out = new Out(coveragePathsOutputPath);
            List<int[]> executionPaths = out.getInstructionPaths();

            Collection<String> fullyQualifiedMethodSignatures = extractMethodSignatures(blockMap.values());
            String methodSignature = fullyQualifiedMethodSignatures.iterator().next();

            Map<String, List<int[]>> instructionPathsByMethod = new HashMap<>();
            instructionPathsByMethod.put(SootMethodEncoder.toJvmMethodFullName(methodSignature), executionPaths);

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
        Path outputPath = null;
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
