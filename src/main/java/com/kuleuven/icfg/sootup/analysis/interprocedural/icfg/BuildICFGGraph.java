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

package com.kuleuven.icfg.sootup.analysis.interprocedural.icfg;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2022 Kadiray Karakaya and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.*;

import com.kuleuven.icfg.coverage.BlockCoverageMap;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.callgraph.CallGraph;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * Helper of slightly modified version of SootUp's v2.0.1 JimpleBasedInterproceduralCFG to support visualization of
 * weighted nodes in ICFGs.
 */
public class BuildICFGGraph {

    private final View view;
    private final JimpleBasedInterproceduralCFG icfg;
    private final BlockCoverageMap blockCoverageMap;

    public BuildICFGGraph(View view, JimpleBasedInterproceduralCFG icfg) {
        this.view = view;
        this.icfg = icfg;
        this.blockCoverageMap = null;
    }

    public BuildICFGGraph(
            View view,
            JimpleBasedInterproceduralCFG icfg,
            BlockCoverageMap blockCoverageMap) {
        this.view = view;
        this.icfg = icfg;
        this.blockCoverageMap = blockCoverageMap;
    }

    public String buildICFGGraph(boolean compact) {
        Map<MethodSignature, ControlFlowGraph<?>> signatureToControlFlowGraph = new LinkedHashMap<>();
        CallGraph cg = icfg.getCg();
        computeAllCalls(cg.getEntryMethods(), signatureToControlFlowGraph, cg);
        return ICFGDotExporter.buildICFGGraph(signatureToControlFlowGraph, view, cg, compact, blockCoverageMap);
    }

    public void computeAllCalls(
            List<MethodSignature> entryPoints,
            Map<MethodSignature, ControlFlowGraph<?>> signatureToControlFlowGraph,
            CallGraph callGraph) {
        ArrayList<MethodSignature> visitedMethods = new ArrayList<>();
        computeAllCalls(entryPoints, signatureToControlFlowGraph, callGraph, visitedMethods);
    }

    private void computeAllCalls(
            List<MethodSignature> entryPoints,
            Map<MethodSignature, ControlFlowGraph<?>> signatureToControlFlowGraph,
            CallGraph callGraph,
            List<MethodSignature> visitedMethods) {
        visitedMethods.addAll(entryPoints);
        for (MethodSignature methodSignature : entryPoints) {
            final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
            // return if the methodSignature is already added to the hashMap to avoid stackoverflow error.
            if (signatureToControlFlowGraph.containsKey(methodSignature)) {
                return;
            }
            if (methodOpt.isPresent()) {
                SootMethod sootMethod = methodOpt.get();
                if (sootMethod.hasBody()) {
                    ControlFlowGraph<?> controlFlowGraph = sootMethod.getBody().getControlFlowGraph();
                    signatureToControlFlowGraph.put(methodSignature, controlFlowGraph);
                }
            }
            callGraph.callTargetsFrom(methodSignature).stream()
                    .filter(methodSignature1 -> !visitedMethods.contains(methodSignature1))
                    .forEach(
                            nextMethodSignature ->
                                    computeAllCalls(
                                            Collections.singletonList(nextMethodSignature),
                                            signatureToControlFlowGraph,
                                            callGraph,
                                            visitedMethods));
        }
    }
}

