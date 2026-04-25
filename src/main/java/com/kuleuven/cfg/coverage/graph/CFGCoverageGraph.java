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

package com.kuleuven.cfg.coverage.graph;

import com.kuleuven.cfg.coverage.graph.util.CoverageGraphToDotConverter;
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
