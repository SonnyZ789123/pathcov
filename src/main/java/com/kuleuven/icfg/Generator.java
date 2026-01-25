package com.kuleuven.icfg;

import com.kuleuven.cg.ReducedCallGraph;
import com.kuleuven.icfg.sootup.analysis.interprocedural.icfg.BuildICFGGraph;
import org.jspecify.annotations.Nullable;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.callgraph.CallGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class Generator {
    private final JavaView view;
    private final JavaSootMethod method;
    private final JimpleBasedInterproceduralCFG icfg;
    @Nullable private final List<String> projectPrefixes;

    public Generator(String classPath, String fullyQualifiedMethodSignature, @Nullable List<String> projectPrefixes) {
        this.projectPrefixes = projectPrefixes;

        // Load classes from the given classpath
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath);
        this.view = new JavaView(inputLocation);

        /*
         * Create the exact method signature in SootUp form.
         * If this does not match exactly, SootUp cannot find the method.
         * The fully qualified method signature is expected to be in the format:
         * <packageName.classType: void main(java.lang.String[])>
         */
        MethodSignature methodSignature = view.getIdentifierFactory().parseMethodSignature(fullyQualifiedMethodSignature);

        Optional<JavaSootMethod> opt = view.getMethod(methodSignature);
        if (opt.isEmpty()) {
            System.err.println("❌ Method not found: " + fullyQualifiedMethodSignature);
            System.exit(1);
        }

        this.method = opt.get();

        this.icfg = new JimpleBasedInterproceduralCFG(
                view,
                Collections.singletonList(method.getSignature()),
                false, false);
    }

    public JimpleBasedInterproceduralCFG getICfg() {
        return icfg;
    }

    public JavaView getView() {
        return view;
    }

    public JavaSootMethod getMethod() {
        return method;
    }

    public String dotExport() {
        CallGraph callGraph = getICfg().getCg();
        ReducedCallGraph reducedCallGraph = new ReducedCallGraph(callGraph, projectPrefixes);

        BuildICFGGraph builder = new BuildICFGGraph(view, getICfg(), reducedCallGraph);

        return builder.buildICFGGraph(false);
    }
}
