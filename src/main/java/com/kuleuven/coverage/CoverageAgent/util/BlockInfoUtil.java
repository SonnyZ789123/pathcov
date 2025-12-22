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
