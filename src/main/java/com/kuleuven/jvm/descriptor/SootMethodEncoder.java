package com.kuleuven.jvm.descriptor;

import sootup.core.model.SootMethod;
import sootup.core.types.*;

public final class SootMethodEncoder {
    public static String toJvmMethodDescriptor(SootMethod method) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');

        for (Type param : method.getParameterTypes()) {
            sb.append(toJvmTypeDescriptor(param));
        }

        sb.append(')');
        sb.append(toJvmTypeDescriptor(method.getReturnType()));

        return sb.toString();
    }

    private static String toJvmTypeDescriptor(Type type) {
        if (type instanceof PrimitiveType p) {
            return primitiveToDescriptor(p);
        }

        if (type instanceof VoidType) {
            return "V";
        }

        if (type instanceof ClassType c) {
            // e.g. java.lang.String → Ljava/lang/String;
            return "L" + c.getFullyQualifiedName().replace('.', '/') + ";";
        }

        if (type instanceof ArrayType a) {
            // JVM arrays: [<component>
            return "[" + toJvmTypeDescriptor(a.getBaseType());
        }

        throw new IllegalArgumentException(
                "Unsupported SootUp type: " + type + " (" + type.getClass() + ")"
        );
    }

    private static String primitiveToDescriptor(PrimitiveType p) {
        return switch (p.getName()) {
            case "boolean" -> "Z";
            case "byte"    -> "B";
            case "char"    -> "C";
            case "short"   -> "S";
            case "int"     -> "I";
            case "long"    -> "J";
            case "float"   -> "F";
            case "double"  -> "D";
            default -> throw new IllegalArgumentException(
                    "Unknown primitive type: " + p.getName()
            );
        };
    }

    private SootMethodEncoder() {}
}

