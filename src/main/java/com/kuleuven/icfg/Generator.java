package com.kuleuven.icfg;

import com.kuleuven.cg.ReducedCallGraph;
import com.kuleuven.icfg.sootup.analysis.interprocedural.icfg.BuildICFGGraph;
import org.jspecify.annotations.Nullable;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

import java.util.List;


public class Generator {
    private final JavaView view;
    private final JavaSootMethod method;
    private final JimpleBasedInterproceduralCFG icfg;

    public Generator(String classPath, String fullyQualifiedMethodSignature, @Nullable List<String> projectPrefixes) {
        com.kuleuven.cg.Generator cgGenerator = new com.kuleuven.cg.Generator(classPath, fullyQualifiedMethodSignature, projectPrefixes);

        ReducedCallGraph reducedCallGraph = cgGenerator.getCallGraph();

        this.view = cgGenerator.getView();
        this.method = cgGenerator.getMethod();

        this.icfg = new JimpleBasedInterproceduralCFG(reducedCallGraph, view,
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
        BuildICFGGraph builder = new BuildICFGGraph(view, getICfg());

        return builder.buildICFGGraph(false);
    }
}
