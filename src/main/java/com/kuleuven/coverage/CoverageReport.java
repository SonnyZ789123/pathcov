package com.kuleuven.coverage;

import com.kuleuven.coverage.intellij.model.coverage.ClassCoverage;
import com.kuleuven.coverage.intellij.model.coverage.CoverageReportJson;
import com.kuleuven.coverage.intellij.model.coverage.MethodCoverage;

public class CoverageReport {
    private final CoverageReportJson coverageReport;

    public CoverageReport(CoverageReportJson coverageReport) {
        this.coverageReport = coverageReport;
    }

    public CoverageReportJson getCoverageReport() {
        return coverageReport;
    }

    public MethodCoverage getForMethodFullName(String methodFullName) {
        for (ClassCoverage classCoverage : coverageReport.classes) {
            String jvmClassName = classCoverage.name.replace('.', '/');

            for (MethodCoverage methodCoverage : classCoverage.methods) {
                String otherMethodFullName = jvmClassName + "." + methodCoverage.methodSignature;

                System.out.println(methodFullName + " -> " + otherMethodFullName);

                if (otherMethodFullName.equals(methodFullName)) {
                    return methodCoverage;
                }
            }
        }
        return null;
    }
}
