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
