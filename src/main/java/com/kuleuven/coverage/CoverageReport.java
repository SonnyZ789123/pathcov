/*
 * Copyright (c) 2025-2026 Yoran Mertens
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
