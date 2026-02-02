package com.kuleuven.coverage;

import com.kuleuven.coverage.model.ClassDTO;
import com.kuleuven.coverage.model.CoverageReportDTO;
import com.kuleuven.coverage.model.LineDTO;
import com.kuleuven.coverage.model.MethodDTO;

import java.util.HashMap;
import java.util.Map;

public class CoverageReport {
    private final CoverageReportDTO coverageReport;
    private Map<Integer, LineDTO> lineToCoverageMap;

    public CoverageReport(CoverageReportDTO coverageReport) {
        this.coverageReport = coverageReport;
    }

    private void buildLineToCoverageMap() {
        lineToCoverageMap = new HashMap<>();
        for (ClassDTO classCoverage : coverageReport.classes) {
            for (MethodDTO methodCoverage : classCoverage.methods) {
                for (LineDTO lineCoverage : methodCoverage.lines) {
                    lineToCoverageMap.put(lineCoverage.line, lineCoverage);
                }
            }
        }
    }

    public CoverageReportDTO getCoverageReport() {
        return coverageReport;
    }

    public Map<Integer, LineDTO> getLineToCoverageMap() {
        if (lineToCoverageMap == null) {
            buildLineToCoverageMap();
        }
        return lineToCoverageMap;
    }

    public MethodDTO getForMethodFullName(String methodFullName) {
        for (ClassDTO classCoverage : coverageReport.classes) {
            String jvmClassName = classCoverage.name.replace('.', '/');

            for (MethodDTO methodCoverage : classCoverage.methods) {
                String otherMethodFullName = jvmClassName + "." + methodCoverage.methodSignature;

                if (otherMethodFullName.equals(methodFullName)) {
                    return methodCoverage;
                }
            }
        }
        return null;
    }
}
