package com.kuleuven.blockmap;

import com.kuleuven.coverage.CoverageReport;
import org.jspecify.annotations.NonNull;
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
            JavaView view, JimpleBasedInterproceduralCFG icfg, CoverageReport coverageReport) {
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
                throw new IllegalStateException("❌ Method not found: " + methodSignature);
            }

            SootMethod method = opt.get();
            methods.add(method);
        });
        return methods;
    }
}
