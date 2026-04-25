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
