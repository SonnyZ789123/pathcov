package com.kuleuven.coverage;

import com.kuleuven.coverage.model.ClassDTO;
import com.kuleuven.coverage.model.CoverageReportDTO;
import com.kuleuven.coverage.model.MethodDTO;

public class CoverageReport {
    private final CoverageReportDTO coverageReport;

    public CoverageReport(CoverageReportDTO coverageReport) {
        this.coverageReport = coverageReport;
    }

    public CoverageReportDTO getCoverageReport() {
        return coverageReport;
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
