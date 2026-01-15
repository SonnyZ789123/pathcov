package com.kuleuven.icfg;

import com.kuleuven.icfg.sootup.analysis.interprocedural.icfg.BuildICFGGraph;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

import java.util.Collections;
import java.util.Optional;


public class Generator {
    public JavaView view;
    public JavaSootMethod method;
    private JimpleBasedInterproceduralCFG icfg;

    public Generator(String classPath, String fullyQualifiedMethodSignature) {
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
    }

    public JimpleBasedInterproceduralCFG getICfg() {
        if (icfg != null) {
            return icfg;
        }

        icfg = new JimpleBasedInterproceduralCFG(
                view,
                Collections.singletonList(method.getSignature()),
                false, false);

        return icfg;
    }

    public String dotExport() {
        BuildICFGGraph builder = new BuildICFGGraph(view, getICfg());
        return builder.buildICFGGraph(false);
    }
}
