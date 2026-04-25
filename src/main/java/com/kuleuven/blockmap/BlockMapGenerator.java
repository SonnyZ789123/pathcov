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

package com.kuleuven.blockmap;

import com.kuleuven.blockmap.model.BlockMapDTO;
import com.kuleuven.coverage.CoverageReport;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.callgraph.CallGraph;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

import java.util.*;

public class BlockMapGenerator {

    public static BlockMapDTO generateBlockMap(
            JavaView view, JimpleBasedInterproceduralCFG icfg, @Nullable CoverageReport coverageReport) {
        Set<SootMethod> methods = getSootMethods(view, icfg);

        Map<SootMethod, ControlFlowGraph<?>> methodToCfgMap = new HashMap<>();
        for (SootMethod method : methods) {
            ControlFlowGraph<?> cfg = icfg.getOrCreateControlFlowGraph(method);
            methodToCfgMap.put(method, cfg);
        }

        BlockMapBuilder blockMapBuilder = new BlockMapBuilder(methodToCfgMap, coverageReport);
        return blockMapBuilder.build();
    }

    private static @NonNull Set<SootMethod> getSootMethods(JavaView view, JimpleBasedInterproceduralCFG icfg) {
        CallGraph cg = icfg.getCg();
        Set<MethodSignature> methodSignatures = cg.getMethodSignatures();

        Set<SootMethod> methods = new HashSet<>();
        methodSignatures.forEach(methodSignature -> {
            Optional<JavaSootMethod> opt = view.getMethod(methodSignature);
            if (opt.isEmpty()) {
                // Could be a method from a superclass, so the method signature is not correct.
                // TODO: In the reduced call graph, we should also filter the inferred methods from java and
                // blacklisted  classes to avoid this problem.
                return;
            }

            SootMethod method = opt.get();
            methods.add(method);
        });
        return methods;
    }
}
