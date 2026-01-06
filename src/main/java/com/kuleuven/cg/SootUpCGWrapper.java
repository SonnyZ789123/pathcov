package com.kuleuven.cg;

import com.kuleuven.config.AppConfig;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;

import java.util.List;
import java.util.Set;

public class SootUpCGWrapper implements ICallGraph<MethodSignature> {
    private final CallGraph callGraph;
    private final Set<MethodSignature> nodes;
    private final Set<SootUpCallWrapper> edges;

    public SootUpCGWrapper(CallGraph callGraph) {
        String[] projectPrefixes = AppConfig.get("project.prefixes").split(",");

        ProjectMethodFilter filter = new ProjectMethodFilter(List.of(projectPrefixes));
        this.callGraph = callGraph;

        Set<MethodSignature> methodSignatures = callGraph.getMethodSignatures();
        Set<CallGraph.Call> calls = callGraph.getCalls();

        this.nodes = filter.filterMethods(methodSignatures);
        this.edges = filter.filterCalls(calls).stream()
                .map(SootUpCallWrapper::new)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public Set<MethodSignature> getNodes() {
        return this.nodes;
    }

    @Override
    public Set<? extends Edge<MethodSignature>> getEdges() {
        return this.edges;
    }

    @Override
    public Set<? extends Edge<MethodSignature>> callsFrom(MethodSignature node) {
        return callGraph.callsFrom(node).stream()
                .map(SootUpCallWrapper::new)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public Set<? extends Edge<MethodSignature>> callsTo(MethodSignature node) {
        return callGraph.callsTo(node).stream()
                .map(SootUpCallWrapper::new)
                .collect(java.util.stream.Collectors.toSet());
    }

    public CallGraph getSootUpCallGraph() {
        return this.callGraph;
    }

    private static class SootUpCallWrapper implements Edge<MethodSignature> {
        CallGraph.Call call;

        private SootUpCallWrapper(CallGraph.Call call) {
            this.call = call;
        }

        @Override
        public MethodSignature getSource() {
            return call.sourceMethodSignature();
        }

        @Override
        public MethodSignature getTarget() {
            return call.targetMethodSignature();
        }
    }
}
