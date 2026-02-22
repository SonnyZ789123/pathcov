package com.kuleuven.blockmap.diff;

import com.github.javaparser.quality.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.blockmap.model.BlockMapDTO;
import com.kuleuven.config.AppConfig;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class GenerateBlockHashTreeDiff {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: previousBlockMap           (e.g., "./out/previous_block_map.json")
         *   1: currentBlockMap            (e.g., "./out/current_block_map.json")
         *   2: outputPath                 (e.g., "./out/cfg_diff.json")
         */
        if (args.length < 2) {
            System.out.println("Expects args <previousBlockMap> <currentBlockMap> [outputPath]");
            System.exit(1);
        }

        String previousBlockMapPath = args[0];
        String currentBlockMapPath = args[1];
        String outputPath = args.length >= 3 ? args[2] : null;

        BlockMapDTO previousBlockMap = readBlockMapFromJson(previousBlockMapPath);
        BlockMapDTO currentBlockMap = readBlockMapFromJson(currentBlockMapPath);

        try {
            BlockHashTreeDiff blockHashTreeDiff = new BlockHashTreeDiff(previousBlockMap, currentBlockMap);
            BlockHashTreeDiff.DiffResult diffResult = blockHashTreeDiff.diff();

            Gson gson = new GsonBuilder().create();
            String diffResultAsJson = gson.toJson(diffResult);

            writeOutputs(diffResultAsJson, outputPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to write coverage graph: " + e.getMessage());
            System.exit(1);
        }
    }

    private static BlockMapDTO readBlockMapFromJson(String blockMapPath) {
        BlockMapDTO blockMap = null;
        try (FileReader reader = new FileReader(Path.of(blockMapPath).toFile())) {
            Gson gson = new GsonBuilder().create();
            blockMap = gson.fromJson(reader, BlockMapDTO.class);
        } catch (IOException e) {
            System.err.println("❌ Failed to read block map from path " + blockMapPath);
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return blockMap;
    }

    private static void writeOutputs(String cfgDiff, @Nullable String outputPath) throws IOException {
        String writeOutputPath = outputPath != null ? outputPath : AppConfig.get("icfg.block_map.diff.write.path");

        Path output = Path.of(writeOutputPath);
        Files.createDirectories(output.getParent());

        try (FileWriter writer = new FileWriter(output.toFile())) {
            writer.write(cfgDiff);
            System.out.println("✅ Block hash tree diff written to " + output);
        }
    }
}
