package com.kuleuven.coverage.intellij.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.rt.coverage.data.ProjectData;
import com.kuleuven.coverage.intellij.loader.IntelliJCoverageLoader;
import com.kuleuven.coverage.intellij.mapper.ProjectDataMapper;
import com.kuleuven.coverage.intellij.model.coverage.CoverageReportJson;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;


public final class CoverageExportMain {

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Expects argument <config.json>");
            System.exit(1);
        }

        Path configPath = Path.of(args[0]);

        // Load coverage and build DTO model
        ProjectData projectData = IntelliJCoverageLoader.loadFromConfig(configPath);

        CoverageReportJson report = ProjectDataMapper.mapSimple(projectData);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        Path outputPath = IntelliJCoverageLoader.getOutputJsonPath(configPath);
        Files.createDirectories(outputPath.getParent());

        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            gson.toJson(report, writer);
        }

        System.out.println("✅ Coverage exported to: " + outputPath.toAbsolutePath());
    }
}
