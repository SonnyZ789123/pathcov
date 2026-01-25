package com.kuleuven.cg;

import org.jspecify.annotations.Nullable;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectMethodFilter {
    private final List<String> projectPrefixes = new ArrayList<>();
    private final List<String> blacklistPrefixes = List.of(
            "java.",
            "javax.",
            "sun.",
            "com.sun.",
            "jdk.",
            "org.w3c.",
            "org.xml.",
            "org.omg.",
            "org.junit.",
            "org.testng.",
            "org.hamcrest.",
            "org.mockito.",
            "org.apache.",
            "com.google.",
            "com.fasterxml.",
            "org.slf4j.",
            "ch.qos.logback."
    );

    public ProjectMethodFilter(@Nullable List<String> projectPrefixes) {
        if (projectPrefixes == null) {
            return;
        }
        // Replace possible "/" with "."
        for (String prefix : projectPrefixes) {
            this.projectPrefixes.add(prefix.replace('/', '.').trim());
        }
    }

    public Stream<MethodSignature> filterMethods(Collection<MethodSignature> methodSignatures) {
        return methodSignatures.stream().filter(this::isProjectMethod);
    }

    public Set<CallGraph.Call> filterCalls(Set<CallGraph.Call> calls) {
        return calls.stream()
                .filter(call -> isProjectMethod(call.sourceMethodSignature()) && isProjectMethod(call.targetMethodSignature()))
                .collect(Collectors.toSet());
    }

    private boolean isProjectMethod(MethodSignature sig) {
        String className = sig.getDeclClassType().getFullyQualifiedName();

        if (!projectPrefixes.isEmpty()) {
            for (String prefix : projectPrefixes) {
                if (className.startsWith(prefix)) {
                    return true;
                }
            }
        } else {
            for (String prefix : blacklistPrefixes) {
                if (className.startsWith(prefix)) {
                    return false;
                }
            }
        }

        return true;
    }

    public Stream<String> filterMethodsFromDotFile(Stream<String> dotFileStream) {
        return dotFileStream.filter(line -> {

            // Keep non-edge lines (headers, closing brace, etc.)
            if (!line.contains("->")) {
                return true;
            }

            // Extract quoted strings "<A>" -> "<B>"
            int q1 = line.indexOf('"');
            int q2 = line.indexOf('"', q1 + 1);
            int q3 = line.indexOf('"', q2 + 1);
            int q4 = line.indexOf('"', q3 + 1);

            if (q1 < 0 || q2 < 0 || q3 < 0 || q4 < 0) {
                return true; // malformed line, keep it
            }

            String srcRaw = line.substring(q1 + 1, q2);
            String tgtRaw = line.substring(q3 + 1, q4);

            // Extract class names (e.g. com.kuleuven.library.Main)
            String srcClass = extractClassName(srcRaw);
            String tgtClass = extractClassName(tgtRaw);

            // Keep only if both src and target belong to the project
            return isProjectClassName(srcClass) && isProjectClassName(tgtClass);
        });
    }

    private String extractClassName(String rawMethod) {
        // rawMethod example:
        // <com.kuleuven.library.Main: void main(java.lang.String[])>

        // Remove surrounding <>
        if (rawMethod.startsWith("<") && rawMethod.endsWith(">")) {
            rawMethod = rawMethod.substring(1, rawMethod.length() - 1);
        }

        // Everything before the colon is the class FQN
        int colonIdx = rawMethod.indexOf(':');
        if (colonIdx == -1) {
            return ""; // malformed -> treat as non-project
        }

        return rawMethod.substring(0, colonIdx).trim();
    }

    private boolean isProjectClassName(String className) {
        if (!projectPrefixes.isEmpty()) {
            return projectPrefixes.stream().anyMatch(className::startsWith);
        }
        return blacklistPrefixes.stream().noneMatch(className::startsWith);
    }
}
