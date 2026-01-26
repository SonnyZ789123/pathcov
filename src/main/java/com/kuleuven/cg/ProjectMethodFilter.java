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

            return false;
        } else {
            // Default: omit standard library and well-known third-party libs
            for (String prefix : blacklistPrefixes) {
                if (className.startsWith(prefix)) {
                    return false;
                }
            }

            return true;
        }
    }
}
