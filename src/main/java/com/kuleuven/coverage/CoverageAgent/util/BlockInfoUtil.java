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

package com.kuleuven.coverage.CoverageAgent.util;

import com.kuleuven.coverage.CoverageAgent.shared.BlockInfo;
import com.kuleuven.jvm.descriptor.JvmDescriptorParser;

import java.util.*;

public final class BlockInfoUtil {

    public static Collection<String> extractMethodSignatures(Collection<BlockInfo> blockInfoSet) {
        Map<Integer, String> methodSignatures = new HashMap<>();

        for (BlockInfo blockInfo : blockInfoSet) {
            int methodKey = getMethodKey(blockInfo);
            methodSignatures.computeIfAbsent(methodKey, (key) -> extractMethodSignature(blockInfo));
        }

        return methodSignatures.values();
    }

    private static int getMethodKey(BlockInfo blockInfo) {
        return Objects.hash(blockInfo.className(), blockInfo.methodName(), blockInfo.methodDescriptor());
    }

    public static String extractMethodSignature(BlockInfo blockInfo) {
        JvmDescriptorParser.ParsedMethodDescriptor methodDesc = JvmDescriptorParser.parseMethodDescriptor(blockInfo.methodDescriptor());

        return String.format(
                "<%s: %s %s(%s)>",
                blockInfo.className(),
                methodDesc.returnType(),
                blockInfo.methodName(),
                String.join(", ", methodDesc.parameterTypes().stream().map(Object::toString).toList())
        );
    }
}
