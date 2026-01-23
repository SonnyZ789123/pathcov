package com.kuleuven.jvm.descriptor;

import sootup.core.model.SootMethod;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;
import sootup.java.core.types.JavaClassType;

import java.util.ArrayList;
import java.util.List;

public final class SootMethodEncoder {
    /**
     * Converts a fully qualified method signature (e.g., \<com.java.Foo: int bar(int)>)
     * to its JVM method full name (e.g., com/java/Foo.bar(I)I).
     *
     * @param fullyQualifiedMethodSignature The fully qualified method signature.
     * @return The JVM method full name.
     */
    public static String toJvmMethodFullName(String fullyQualifiedMethodSignature) {
        String s = fullyQualifiedMethodSignature.trim();
        s = s.substring(1, s.length() - 1); // remove < >

        int colon = s.indexOf(':');
        String className = s.substring(0, colon).trim();
        String jvmClassName = className.replace('.', '/');
        String rest = s.substring(colon + 1).trim();

        int space = rest.indexOf(' ');
        String returnTypeStr = rest.substring(0, space).trim();
        String nameAndParams = rest.substring(space + 1).trim();

        int lparen = nameAndParams.indexOf('(');
        int rparen = nameAndParams.lastIndexOf(')');

        String methodName = nameAndParams.substring(0, lparen).trim();
        String paramsStr = nameAndParams.substring(lparen + 1, rparen).trim();

        List<Type> paramTypes = new ArrayList<>();
        if (!paramsStr.isEmpty()) {
            for (String p : paramsStr.split(",")) {
                paramTypes.add(parseType(p.trim()));
            }
        }

        Type returnType = parseType(returnTypeStr);

        String descriptor = toJvmMethodDescriptor(paramTypes, returnType);

        return jvmClassName + "." + methodName + descriptor;
    }


    public static String toJvmMethodDescriptor(SootMethod method) {
        return toJvmMethodDescriptor(
                method.getParameterTypes(),
                method.getReturnType()
        );
    }

    public static String toJvmMethodDescriptor(
            List<Type> parameterTypes,
            Type returnType
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Type param : parameterTypes) {
            sb.append(toJvmTypeDescriptor(param));
        }
        sb.append(')');
        sb.append(toJvmTypeDescriptor(returnType));
        return sb.toString();
    }

    private static Type parseType(String t) {
        int dims = 0;
        while (t.endsWith("[]")) {
            dims++;
            t = t.substring(0, t.length() - 2);
        }

        Type base = switch (t) {
            case "boolean" -> PrimitiveType.BooleanType.getInstance();
            case "byte"    -> PrimitiveType.ByteType.getInstance();
            case "char"    -> PrimitiveType.CharType.getInstance();
            case "short"   -> PrimitiveType.ShortType.getInstance();
            case "int"     -> PrimitiveType.IntType.getInstance();
            case "long"    -> PrimitiveType.LongType.getInstance();
            case "float"   -> PrimitiveType.FloatType.getInstance();
            case "double"  -> PrimitiveType.DoubleType.getInstance();
            case "void"    -> VoidType.getInstance();
            default -> toJavaClassType(t);
        };

        if (dims == 0) {
            return base;
        }

        return new ArrayType(base, dims);
    }

    private static JavaClassType toJavaClassType(String fqn) {
        int lastDot = fqn.lastIndexOf('.');
        if (lastDot < 0) {
            // default package
            return new JavaClassType(fqn, PackageName.DEFAULT_PACKAGE);
        }

        String pkg = fqn.substring(0, lastDot);
        String cls = fqn.substring(lastDot + 1);

        return new JavaClassType(cls, new PackageName(pkg));
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

