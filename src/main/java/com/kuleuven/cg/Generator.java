package com.kuleuven.cg;

import org.jspecify.annotations.Nullable;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
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
    private final ReducedCallGraph callGraph;

    public Generator(String classPath, String fullyQualifiedMethodSignature, @Nullable List<String> projectPrefixes) {
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

        CallGraph cg = (new ClassHierarchyAnalysisAlgorithm(view))
                .initialize(Collections.singletonList(method.getSignature()));

        // Reduce the call graph to project-specific classes if prefixes are provided
        callGraph = new ReducedCallGraph(cg, projectPrefixes);
    }

    public ReducedCallGraph getCallGraph() {
        return callGraph;
    }

    public JavaView getView() {
        return view;
    }

    public JavaSootMethod getMethod() {
        return method;
    }
}
