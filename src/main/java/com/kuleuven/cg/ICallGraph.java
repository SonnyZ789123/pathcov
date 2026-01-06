package com.kuleuven.cg;

import java.util.Set;

public interface ICallGraph<NodeType> {

    Set<NodeType> getNodes();

    Set<? extends Edge<NodeType>> getEdges();

    Set<? extends Edge<NodeType>> callsFrom(NodeType node);

    Set<? extends Edge<NodeType>> callsTo(NodeType node);

    interface Edge<N> {
        N getSource();
        N getTarget();
    }
}
