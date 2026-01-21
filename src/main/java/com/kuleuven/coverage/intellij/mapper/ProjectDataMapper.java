package com.kuleuven.coverage.intellij.mapper;

import com.intellij.rt.coverage.data.*;
import com.intellij.rt.coverage.data.instructions.ClassInstructions;
import com.intellij.rt.coverage.data.instructions.JumpInstructions;
import com.intellij.rt.coverage.data.instructions.LineInstructions;
import com.intellij.rt.coverage.data.instructions.SwitchInstructions;
import com.intellij.rt.coverage.util.ArrayUtil;
import com.kuleuven.coverage.intellij.model.*;

import java.util.List;

public final class ProjectDataMapper {

    public static CoverageReportDTO map(ProjectData projectData) {
        CoverageReportDTO report = new CoverageReportDTO();

        for (ClassData classData : projectData.getClassesCollection()) {
            LineData[] lines = (LineData[]) classData.getLines();
            if (lines == null) continue;

            ClassDTO cls = new ClassDTO();
            cls.name = classData.getName();

            ClassInstructions ci =
                    projectData.getInstructions().get(classData.getName());
            LineInstructions[] lineInstrs =
                    ci == null ? null : ci.getlines();

            for (LineData line : lines) {
                if (line == null) continue;

                LineDTO l = new LineDTO();
                l.line = line.getLineNumber();
                l.hits = line.getHits();

                // Branch summary
                BranchData bd = line.getBranchData();
                l.branches = new BranchSummaryDTO();
                l.branches.total = bd == null ? 0 : bd.getTotalBranches();
                l.branches.covered = bd == null ? 0 : bd.getCoveredBranches();

                if (lineInstrs != null) {
                    LineInstructions li = ArrayUtil.safeLoad(lineInstrs, l.line);
                    if (li != null) {

                        // Instruction summary
                        BranchData id = li.getInstructionsData(line);
                        l.instructions = new InstructionSummaryDTO();
                        l.instructions.straight = li.getInstructions();
                        l.instructions.total = id.getTotalBranches();
                        l.instructions.covered = id.getCoveredBranches();

                        // Jumps
                        JumpData[] jumps = line.getJumps();
                        List<JumpInstructions> jis = li.getJumps();
                        if (jumps != null && jis != null) {
                            for (int i = 0; i < Math.min(jumps.length, jis.size()); i++) {
                                JumpDTO j = new JumpDTO();
                                j.index = i;

                                JumpData jd = jumps[i];
                                JumpInstructions ji = jis.get(i);

                                j.trueBranch = new OutcomeDTO();
                                j.trueBranch.instr = ji.getInstructions(true);
                                j.trueBranch.hits = jd.getTrueHits();

                                j.falseBranch = new OutcomeDTO();
                                j.falseBranch.instr = ji.getInstructions(false);
                                j.falseBranch.hits = jd.getFalseHits();

                                l.jumps.add(j);
                            }
                        }

                        // Switches
                        SwitchData[] sds = line.getSwitches();
                        List<SwitchInstructions> sis = li.getSwitches();
                        if (sds != null && sis != null) {
                            for (int i = 0; i < Math.min(sds.length, sis.size()); i++) {
                                SwitchDTO s = new SwitchDTO();
                                s.index = i;

                                SwitchData sd = sds[i];
                                SwitchInstructions si = sis.get(i);

                                s.defaultBranch = new OutcomeDTO();
                                s.defaultBranch.instr = si.getInstructions(-1);
                                s.defaultBranch.hits = sd.getDefaultHits();

                                for (int k = 0; k < sd.getKeys().length; k++) {
                                    CaseDTO c = new CaseDTO();
                                    c.key = sd.getKeys()[k];
                                    c.instr = si.getInstructions(k);
                                    c.hits = sd.getHits()[k];
                                    s.cases.add(c);
                                }

                                l.switches.add(s);
                            }
                        }
                    }
                }

                cls.lines.add(l);
            }

            report.classes.add(cls);
        }

        return report;
    }
}

