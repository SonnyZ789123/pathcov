package com.kuleuven.cg;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SootUpCGWrapper implements ICallGraph<MethodSignature> {
    private final CallGraph callGraph;
    private final Set<MethodSignature> nodes;
    private final Set<SootUpCallWrapper> edges;

    public SootUpCGWrapper(CallGraph callGraph, @Nullable List<String> projectPrefixes) {
        ProjectMethodFilter filter = new ProjectMethodFilter(projectPrefixes);
        this.callGraph = callGraph;

        Set<MethodSignature> methodSignatures = callGraph.getMethodSignatures();
        Set<CallGraph.Call> calls = callGraph.getCalls();

        this.nodes = filter.filterMethods(methodSignatures);
        this.edges = filter.filterCalls(calls).stream()
                .map(SootUpCallWrapper::new)
                .collect(Collectors.toSet());
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
                .collect(Collectors.toSet());
    }

    @Override
    public Set<? extends Edge<MethodSignature>> callsTo(MethodSignature node) {
        return callGraph.callsTo(node).stream()
                .map(SootUpCallWrapper::new)
                .collect(Collectors.toSet());
    }

    @Override
    public @NonNull Set<MethodSignature> callTargetsFrom(@NonNull MethodSignature sourceMethod) {
        return this.callsFrom(sourceMethod).stream().map(Edge::getTarget).collect(Collectors.toSet());
    }

    @Override
    public @NonNull Set<MethodSignature> callSourcesTo(@NonNull MethodSignature targetMethod) {
        return this.callsTo(targetMethod).stream().map(Edge::getSource).collect(Collectors.toSet());
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
