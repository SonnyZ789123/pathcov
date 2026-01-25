package com.kuleuven.cg;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphDifference;
import sootup.callgraph.MutableCallGraph;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.signatures.MethodSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A reduced call graph that only contains methods and calls within the specified project prefixes.
 * See GraphBasedCallGraph of SootUp for more details.
 */
public class ReducedCallGraph implements MutableCallGraph {
    private final CallGraph internalCallGraph;
    private final Set<MethodSignature> methodSignatures;
    private final Set<CallGraph.Call> calls;
    private final List<MethodSignature> entryMethods;
    private final List<String> projectPrefixes;

    public ReducedCallGraph(CallGraph callGraph, @Nullable List<String> projectPrefixes) {
        this.internalCallGraph = callGraph;
        this.projectPrefixes = projectPrefixes;

        ProjectMethodFilter filter = new ProjectMethodFilter(projectPrefixes);
        Set<MethodSignature> originalMethodSignatures = callGraph.getMethodSignatures();
        Set<CallGraph.Call> originalCalls = callGraph.getCalls();

        this.entryMethods = filter.filterMethods(callGraph.getEntryMethods()).collect(Collectors.toList());
        this.methodSignatures = filter.filterMethods(originalMethodSignatures).collect(Collectors.toSet());
        this.calls = filter.filterCalls(originalCalls);
    }

    @Override
    public void addMethod(@NonNull MethodSignature methodSignature) {
        if (containsMethod(methodSignature)) {
            return;
        }

        this.methodSignatures.add(methodSignature);
    }

    @Override
    public void addCall(
            @NonNull MethodSignature sourceMethod,
            @NonNull MethodSignature targetMethod,
            @NonNull InvokableStmt invokableStmt) {
        addCall(new Call(sourceMethod, targetMethod, invokableStmt));
    }

    @Override
    public void addCall(@NonNull Call call) {
        if (!containsMethod(call.sourceMethodSignature())) {
            addMethod(call.sourceMethodSignature());
        }
        if (!containsMethod(call.targetMethodSignature())) {
            addMethod(call.targetMethodSignature());
        }
        calls.add(call);
    }

    @Override
    public @NonNull Set<MethodSignature> getMethodSignatures() {
        return methodSignatures;
    }

    @Override
    public @NonNull Set<Call> getCalls() {
        return calls;
    }

    @Override
    public @NonNull Set<MethodSignature> callTargetsFrom(@NonNull MethodSignature sourceMethod) {
        return this.callsFrom(sourceMethod).stream()
                .map(CallGraph.Call::targetMethodSignature)
                .collect(Collectors.toSet());
    }

    @Override
    public @NonNull Set<MethodSignature> callSourcesTo(@NonNull MethodSignature targetMethod) {
        return this.callsTo(targetMethod).stream()
                .map(CallGraph.Call::sourceMethodSignature)
                .collect(Collectors.toSet());
    }

    @Override
    public @NonNull Set<Call> callsFrom(@NonNull MethodSignature sourceMethod) {
        return internalCallGraph.callsFrom(sourceMethod);
    }

    @Override
    public @NonNull Set<Call> callsTo(@NonNull MethodSignature targetMethod) {
        return internalCallGraph.callsTo(targetMethod);
    }

    @Override
    public boolean containsMethod(@NonNull MethodSignature methodSignature) {
        return methodSignatures.contains(methodSignature);
    }

    @Override
    public boolean containsCall(
            @NonNull MethodSignature sourceMethod,
            @NonNull MethodSignature targetMethod,
            @NonNull InvokableStmt invokableStmt) {
        if (!containsMethod(sourceMethod) || !containsMethod(targetMethod)) {
            return false;
        }
        return containsCall(new Call(sourceMethod, targetMethod, invokableStmt));
    }

    @Override
    public boolean containsCall(@NonNull Call call) {
        return calls.contains(call);
    }

    @Override
    public int callCount() {
        return calls.size();
    }

    @Override
    public @NonNull MutableCallGraph copy() {
        return new ReducedCallGraph(this.internalCallGraph.copy(), new ArrayList<>(projectPrefixes));
    }

    @Override
    public List<MethodSignature> getEntryMethods() {
        return entryMethods;
    }

    @Override
    public @NonNull CallGraphDifference diff(@NonNull CallGraph callGraph) {
        return new CallGraphDifference(this, callGraph);
    }
}
