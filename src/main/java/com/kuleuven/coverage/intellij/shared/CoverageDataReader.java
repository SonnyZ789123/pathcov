package com.kuleuven.coverage.intellij.shared;

import com.google.gson.Gson;
import com.kuleuven.coverage.CoverageReport;
import com.kuleuven.coverage.model.CoverageReportDTO;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class CoverageDataReader {
    private final CoverageReport coverageReport;

    public CoverageDataReader(String outputPath) throws IOException {
        Gson gson = new Gson();

        try (Reader r = Files.newBufferedReader(Path.of(outputPath))) {
            CoverageReportDTO coverageReportDTO = gson.fromJson(r, CoverageReportDTO.class);
            this.coverageReport = new CoverageReport(coverageReportDTO);
        }
    }

    public CoverageReport getCoverageReport() {
        return coverageReport;
    }
}
