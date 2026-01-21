package com.kuleuven.coverage.intellij.loader;

import com.google.gson.Gson;
import com.intellij.rt.coverage.data.ProjectData;
import com.intellij.rt.coverage.report.ReportLoadStrategy;
import com.intellij.rt.coverage.report.Reporter;
import com.intellij.rt.coverage.report.api.Filters;
import com.intellij.rt.coverage.report.data.BinaryReport;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class IntelliJCoverageLoader {

    // -------------------------
    // Config DTO (internal)
    // -------------------------
    private static final class Config {
        String reportPath;
        List<String> outputRoots;
        List<String> sourceRoots;
        List<String> includeClasses;
        String outputJson;
    }

    public static ProjectData loadFromConfig(Path configPath) throws Exception {
        // 1. Parse config JSON
        Config cfg;
        try (FileReader reader = new FileReader(configPath.toFile())) {
            cfg = new Gson().fromJson(reader, Config.class);
        }

        if (cfg == null) {
            throw new IllegalArgumentException("Invalid config: empty JSON");
        }

        // 2. Resolve paths relative to config file
        Path baseDir = configPath.getParent() == null
                ? Path.of(".")
                : configPath.getParent();

        File reportFile = baseDir.resolve(cfg.reportPath).toFile();
        if (!reportFile.exists()) {
            throw new IllegalArgumentException("Coverage report not found: " + reportFile);
        }

        // 3. Binary reports
        List<BinaryReport> reports =
                Collections.singletonList(new BinaryReport(reportFile, null));

        // 4. Output roots
        List<File> outputRoots = new ArrayList<>();
        for (String p : cfg.outputRoots) {
            outputRoots.add(baseDir.resolve(p).toFile());
        }

        // 5. Source roots
        List<File> sourceRoots = new ArrayList<>();
        for (String p : cfg.sourceRoots) {
            sourceRoots.add(baseDir.resolve(p).toFile());
        }

        // 6. Filters (includeClasses only)
        List<Pattern> include = new ArrayList<>();
        if (cfg.includeClasses != null) {
            for (String regex : cfg.includeClasses) {
                include.add(Pattern.compile(regex));
            }
        }

        Filters filters = new Filters(
                include,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        // 7. Load IntelliJ ProjectData
        ReportLoadStrategy strategy =
                new ReportLoadStrategy.RawReportLoadStrategy(
                        reports,
                        outputRoots,
                        sourceRoots,
                        filters
                );

        Reporter reporter = new Reporter(strategy, "coverage-report");
        return reporter.getProjectData();
    }

    public static Path getOutputJsonPath(Path configPath) throws Exception {
        try (FileReader reader = new FileReader(configPath.toFile())) {
            Config cfg = new Gson().fromJson(reader, Config.class);

            if (cfg.outputJson == null || cfg.outputJson.isEmpty()) {
                throw new IllegalArgumentException("outputJson must be specified in config");
            }

            Path output = Path.of(cfg.outputJson);

            // If already absolute, return as-is
            if (output.isAbsolute()) {
                return output.normalize();
            }

            // Otherwise resolve relative to config file directory
            Path baseDir = configPath.getParent();
            if (baseDir == null) {
                baseDir = Path.of(".");
            }

            return baseDir.resolve(output).normalize();
        }
    }

}
