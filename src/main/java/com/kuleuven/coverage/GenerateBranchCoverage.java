package com.kuleuven.coverage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.blockmap.coverage.BranchCoverage;
import com.kuleuven.blockmap.model.BlockMapDTO;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class GenerateBranchCoverage {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: blockMapPath           (e.g., "./out/icfg_block_map.json")
         */
        if (args.length < 1) {
            System.out.println("Expects args <blockMapPath>");
            System.exit(1);
        }
        String blockMapPath = args[0];

        BlockMapDTO blockMap = null;
        try (FileReader reader = new FileReader(Path.of(blockMapPath).toFile())) {
            Gson gson = new GsonBuilder().create();
            blockMap = gson.fromJson(reader, BlockMapDTO.class);
        } catch (IOException e) {
            System.err.println("❌ Failed to read block map from path " + blockMapPath);
            System.exit(1);
        }


        double branchCoverage = BranchCoverage.calculate(blockMap);
        System.out.println("✅ Branch coverage: " + String.format("%.2f", branchCoverage) + "%");
    }
}
