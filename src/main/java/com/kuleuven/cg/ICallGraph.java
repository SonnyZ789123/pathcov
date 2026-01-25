package com.kuleuven.cg;

import org.jspecify.annotations.NonNull;
import sootup.core.signatures.MethodSignature;

import java.util.List;
import java.util.Set;

public interface ICallGraph<NodeType> {

    Set<NodeType> getNodes();

    Set<? extends Edge<NodeType>> getEdges();

    Set<? extends Edge<NodeType>> callsFrom(NodeType node);

    Set<? extends Edge<NodeType>> callsTo(NodeType node);

    @NonNull Set<MethodSignature> callTargetsFrom(@NonNull MethodSignature sourceMethod);

    @NonNull Set<MethodSignature> callSourcesTo(@NonNull MethodSignature targetMethod);

    List<MethodSignature> getEntryMethods();

    boolean containsMethod(@NonNull MethodSignature method);

    interface Edge<N> {
        N getSource();
        N getTarget();
    }
}
