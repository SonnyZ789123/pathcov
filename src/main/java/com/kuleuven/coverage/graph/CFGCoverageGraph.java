package com.kuleuven.coverage.graph;

import com.kuleuven.coverage.graph.util.CoverageGraphToDotConverter;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;

import java.util.*;

public final class CFGCoverageGraph implements PropertyGraph {
    private final String name;
    private final List<PropertyGraphNode> nodes;
    private final List<PropertyGraphEdge> edges;

    private CFGCoverageGraph(String name, List<PropertyGraphNode> nodes, List<PropertyGraphEdge> edges) {
        this.name = name;
        this.nodes = Collections.unmodifiableList(nodes);
        this.edges = Collections.unmodifiableList(edges);
    }

    public String getName() {
        return this.name;
    }

    public List<PropertyGraphNode> getNodes() {
        return this.nodes;
    }

    public List<PropertyGraphEdge> getEdges() {
        return this.edges;
    }

    public String toDotGraph() {
        return CoverageGraphToDotConverter.convert(this);
    }

    public static class Builder implements PropertyGraph.Builder {
        private final Set<Integer> nodeBlockIds = new HashSet<>();
        private final List<PropertyGraphNode> nodes = new ArrayList<>();
        private final List<PropertyGraphEdge> edges = new ArrayList<>();
        private String name;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder addNode(PropertyGraphNode node) {
            if (!(node instanceof CoverageNode coverageNode)) {
                throw new IllegalArgumentException("Graph can only contain coverage nodes");
            } else {
                if (!this.nodeBlockIds.contains(coverageNode.getBlockInfo().blockId())) {
                    this.nodes.add(node);
                    this.nodeBlockIds.add(coverageNode.getBlockInfo().blockId());
                }

                return this;
            }
        }

        public Builder addEdge(PropertyGraphEdge edge) {
            if (!this.edges.contains(edge)) {
                this.edges.add(edge);
            }

            return this;
        }

        public PropertyGraph build() {
            return new CFGCoverageGraph(this.name, this.nodes, this.edges);
        }
    }
}
