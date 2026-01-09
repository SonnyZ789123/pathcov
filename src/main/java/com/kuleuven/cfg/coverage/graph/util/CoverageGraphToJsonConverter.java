package com.kuleuven.cfg.coverage.graph.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.cfg.coverage.graph.CoverageEdge;
import com.kuleuven.cfg.coverage.graph.CoverageGraph;
import com.kuleuven.cfg.coverage.graph.CoverageNode;

import java.util.HashSet;
import java.util.Set;

public class CoverageGraphToJsonConverter {
    private record Node(String id, String label, boolean covered, double weight) {}

    private record Edge(String from, String to, String label, double weight, Integer branchIdx) {}

    private static final class CoverageGraphJson {
        public final Set<Node> nodes;
        public final Set<Edge> edges;

        public CoverageGraphJson(Set<Node> nodes, Set<Edge> edges) {
            this.nodes = nodes;
            this.edges = edges;
        }
    }

    public static String convert(CoverageGraph graph) {
        Set<Node> nodes = new HashSet<>();
        Set<Edge> edges = new HashSet<>();

        graph.getGraph().getNodes()
                .stream().map(n -> (CoverageNode) n)
                .forEach(node -> {
                    Node jsonNode = new Node(
                            String.valueOf(node.getBlockInfo().blockId()),
                            node.getBlock().getTail().toString(),
                            node.getCoverageCount() > 0,
                            node.getCoverageCount()
                    );

                    nodes.add(jsonNode);
                });

        graph.getGraph().getEdges()
                .stream().map(e -> (CoverageEdge) e)
                .forEach(edge -> {
                    Edge jsonEdge = new Edge(
                            String.valueOf(edge.getSource().getBlockInfo().blockId()),
                            String.valueOf(edge.getDestination().getBlockInfo().blockId()),
                            edge.getLabel(),
                            1,
                            edge.getBranchIndex()
                    );

                    edges.add(jsonEdge);
                });

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(new CoverageGraphJson(nodes, edges));
    }
}
