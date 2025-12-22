package com.kuleuven.jvm.descriptor;

import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.java.core.types.JavaClassType;

import java.util.ArrayList;
import java.util.List;

public final class JvmDescriptorParser {
    public static ParsedMethodDescriptor parseMethodDescriptor(String descriptor) {
        if (descriptor == null || descriptor.isEmpty() || descriptor.charAt(0) != '(') {
            throw new IllegalArgumentException("Invalid JVM method descriptor: " + descriptor);
        }

        int index = 1; // skip '('
        List<Type> parameterTypes = new ArrayList<>();

        while (descriptor.charAt(index) != ')') {
            ParseResult r = parseType(descriptor, index);
            parameterTypes.add(r.type());
            index = r.nextIndex();
        }

        index++; // skip ')'

        ParseResult returnResult = parseType(descriptor, index);

        return new ParsedMethodDescriptor(parameterTypes, returnResult.type());
    }

    private static ParseResult parseType(String desc, int index) {
        char c = desc.charAt(index);

        // array
        if (c == '[') {
            ParseResult base = parseType(desc, index + 1);
            return new ParseResult(
                    new ArrayType(base.type(), 1),
                    base.nextIndex()
            );
        }

        // object
        if (c == 'L') {
            int semicolon = desc.indexOf(';', index);
            if (semicolon == -1) {
                throw new IllegalArgumentException("Invalid object type in descriptor: " + desc);
            }

            String internalName = desc.substring(index + 1, semicolon);
            String fqcn = internalName.replace('/', '.');

            JavaClassType classType = toJavaClassType(fqcn);

            return new ParseResult(
                    classType,
                    semicolon + 1
            );
        }

        // primitive or void
        return new ParseResult(
                primitiveFromDescriptor(c),
                index + 1
        );
    }

    private static JavaClassType toJavaClassType(String fqcn) {
        int lastDot = fqcn.lastIndexOf('.');

        String packageNameStr;
        String className;

        if (lastDot == -1) {
            packageNameStr = "";
            className = fqcn;
        } else {
            packageNameStr = fqcn.substring(0, lastDot);
            className = fqcn.substring(lastDot + 1);
        }

        PackageName packageName = new PackageName(packageNameStr);

        return new JavaClassType(className, packageName);
    }

    private static Type primitiveFromDescriptor(char c) {
        return switch (c) {
            case 'Z' -> PrimitiveType.BooleanType.getInstance();
            case 'B' -> PrimitiveType.ByteType.getInstance();
            case 'C' -> PrimitiveType.CharType.getInstance();
            case 'S' -> PrimitiveType.ShortType.getInstance();
            case 'I' -> PrimitiveType.IntType.getInstance();
            case 'J' -> PrimitiveType.LongType.getInstance();
            case 'F' -> PrimitiveType.FloatType.getInstance();
            case 'D' -> PrimitiveType.DoubleType.getInstance();
            case 'V' -> VoidType.getInstance();
            default -> throw new IllegalArgumentException(
                    "Unknown JVM type descriptor: " + c
            );
        };
    }

    private record ParseResult(Type type, int nextIndex) {}

    public record ParsedMethodDescriptor(
            List<Type> parameterTypes,
            Type returnType
    ) {}
}
