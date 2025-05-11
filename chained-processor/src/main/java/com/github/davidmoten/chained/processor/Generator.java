package com.github.davidmoten.chained.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public final class Generator {

    private Generator() {
        // prevent instantiation
    }

    static String chainedBuilder(String className, String builderClassName, List<Parameter> parameters) {
        Output o = new Output();
        List<Parameter> mandatory = parameters.stream().filter(p -> !p.isOptional()).collect(Collectors.toList());
        List<Parameter> optionals = parameters.stream().filter(p -> p.isOptional()).collect(Collectors.toList());
        final String firstBuilderSimpleClassName = Util.simpleClassName(builderClassName);
        if (mandatory.isEmpty()) {
            return simpleBuilder(className, builderClassName, parameters);
        } else if (optionals.isEmpty() && mandatory.size() == 1) {
            Parameter p = mandatory.get(0);
            o.line("public static %s of(%s %s) {", className, p.type(), p.name());
            o.line("return new %s(%s);", className, p.name());
            o.close();
            return o.toString();
        } else {
            o.line("public static %s builder() {", firstBuilderSimpleClassName);
            o.close();
            o.line();
            {
                Parameter p = mandatory.get(0);
                String nextBuilder = builderClassName(p.name());
                o.line("public static %s %s(%s %s) {", nextBuilder, p.name(), p.type(), p.name());
                o.line("return builder().%s(%s);", p.name(), p.name());
                o.close();
            }
            o.line();
            o.line("public final static class %s {", firstBuilderSimpleClassName);
            o.line();
            for (Parameter p : parameters) {
                if (p.isOptional()) {
                    o.line("private %s %s = %s.empty();", p.type(), wrappingType(p.name()), p.name());
                } else {
                    o.line("private %s %s;", p.type(), p.name());
                }
            }
            privateConstructor(o, firstBuilderSimpleClassName);
            o.line();
            writeMandatorySetter(o, mandatory.get(0));
            o.line();
            o.line("private %s build() {", className);
            String params = parameters.stream().map(x -> x.name()).collect(Collectors.joining(", "));
            o.line("return new %s(%s);", className, params);
            o.close();
            o.close();

            for (int i = 0; i < mandatory.size() - 1; i++) {
                Parameter p = mandatory.get(i);
                String builder = builderClassName(p.name());
                o.line();
                o.line("public final static class %s {", builder);
                o.line();
                o.line("private final %s _b;", firstBuilderSimpleClassName);
                o.line();
                o.line("private %s(%s _b) {", builder, firstBuilderSimpleClassName);
                o.line("this._b = _b;");
                o.close();

                Parameter q = mandatory.get(i + 1);
                if (i + 1 == mandatory.size() - 1 && optionals.isEmpty()) {
                    o.line("public %s %s(%s %s) {", className, q.name(), q.type(), q.name());
                    writeNullCheck(o, q);
                    o.line("this._b.%s = %s;", q.name(), q.name());
                    o.line("return _b.build();");
                    o.close();
                    o.close();
                    return o.toString();
                } else {
                    String nextBuilder = builderClassName(q.name());
                    o.line("public %s %s(%s %s) {", nextBuilder, q.name(), q.type(), q.name());
                    writeNullCheck(o, q);
                    o.line("this._b.%s = %s;", q.name(), q.name());
                    o.line("return new %s(_b);", nextBuilder);
                    o.close();
                    o.close();
                }
            }
            // optionals cannot be empty if get to here
            String lastBuilder = builderClassName(mandatory.get(mandatory.size() - 1).name());
            o.line();
            o.line("public final static class %s {", lastBuilder);
            o.line();
            o.line("private final %s _b;", firstBuilderSimpleClassName);
            o.line();
            o.line("private %s(%s _b) {", lastBuilder, firstBuilderSimpleClassName);
            o.line("this._b = _b;");
            o.close();
            for (Parameter p : optionals) {
                o.line();
                o.line("public %s %s(%s %s) {", lastBuilder, p.name(), toPrimitive(wrappedType(p.type())), p.name());
                writeNullCheck(o, p);
                o.line("this._b.%s = %s.of(%s);", p.name(), wrappingType(p.type()), p.name());
                o.line("return this;");
                o.close();
                o.line();
                o.line("public %s %s(%s %s) {", lastBuilder, p.name(), p.type(), p.name());
                writeNullCheck(o, p);
                o.line("this._b.%s = %s;", p.name(), p.name());
                o.line("return this;");
                o.close();
            }
            o.line();
            o.line("public %s build() {", className);
            o.line("return _b.build();");
            o.close();
            o.close();
            return o.toString();
        }
    }

    private static String wrappedType(String type) {
        int i = type.indexOf("<");
        if (i == -1) {
            return type;
        } else {
            int j = type.lastIndexOf(">");
            if (j == -1) {
                throw new RuntimeException("unexpected lack of closing > in type name: " + type);
            } else {
                return type.substring(i + 1, j);
            }
        }
    }

    private static String wrappingType(String type) {
        int i = type.indexOf("<");
        if (i == -1) {
            return type;
        } else {
            return type.substring(0, i);
        }
    }

    private static void privateConstructor(Output o, String className) {
        o.line();
        o.line("private %s {", className);
        o.line("// prevent instantiation");
        o.close();
    }

    private static void writeMandatorySetter(Output o, Parameter p) {
        String nextBuilder = builderClassName(p.name());
        o.line("public %s %s(%s %s) {", nextBuilder, p.name(), p.type(), p.name());
        writeNullCheck(o, p);
        o.line("this.%s = %s;", p.name(), p.name());
        o.line("return new %s(this);", nextBuilder);
        o.close();
    }

    private static void writeNullCheck(Output o, Parameter p) {
        if (!p.isPrimitive()) {
            o.line("Preconditions.checkNotNull(%s, \"%s\");", p.name(), p.name());
        }
    }

    private static String builderClassName(String field) {
        return "BuilderWith" + upperFirst(field);
    }

    private static String upperFirst(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String simpleBuilder(String className, String builderClassName, List<Parameter> parameters) {
        Output o = new Output().right();
        final String firstBuilderSimpleClassName = Util.simpleClassName(builderClassName);
        o.line("public static %s builder() {", firstBuilderSimpleClassName);
        o.line("return new %s();", firstBuilderSimpleClassName);
        o.close();
        o.line();
        o.line("public final static class %s {", firstBuilderSimpleClassName);
        o.line();
        for (Parameter p : parameters) {
            if (p.isOptional()) {
                o.line("private %s %s = %s.empty();", p.type(), wrappingType(p.type()), p.name());
            } else {
                o.line("private %s %s;", p.type(), p.name());
            }
        }
        privateConstructor(o, "Builder");
        for (Parameter p : parameters) {
            if (p.isOptional()) {
                String wrappedType = wrappedType(p.type());
                wrappedType = toPrimitive(wrappedType);
                o.line();
                o.line("public %s %s(%s %s) {", firstBuilderSimpleClassName, p.name(), wrappedType, p.name());
                o.line("Preconditions.checkNotNull(%s, \"%s\");", p.name(), p.name());
                o.line("this.%s = %s.of(%s);", p.name(), wrappedType(p.type()), p.name());
                o.line("return this;");
                o.close();
            }
            o.line();
            o.line("public %s %s(%s %s) {", firstBuilderSimpleClassName, p.name(), p.type(), p.name());
            o.line("Preconditions.checkNotNull(%s, \"%s\");", p.name(), p.name());
            o.line("this.%s = %s;", p.name(), p.name());
            o.line("return this;");
            o.close();
        }
        o.line();
        o.line("public %s build() {", className);
        String params = parameters.stream().map(p -> p.name()).collect(Collectors.joining(", "));
        o.line("return new %s(%s);", className, params);
        o.close();
        o.close();
        return o.toString();
    }

    public static final class Output {

        private StringBuilder b = new StringBuilder();

        private String indent = "";

        public Output left() {
            indent = indent.substring(0, indent.length() - 4);
            return this;
        }

        public Output right() {
            indent += "    ";
            return this;
        }

        public void line() {
            b.append("\n");
        }

        public void line(String fmt, Object... args) {
            b.append(String.format("\n" + indent + fmt, args));
            if (fmt.endsWith("{")) {
                right();
            }
        }

        public void close() {
            left();
            line("}");
        }

        @Override
        public String toString() {
            return b.toString();
        }
    }

    private static final Map<String, String> TYPE_PRIMITIVES = createPrimitives();
    private static final Set<String> PRIMITIVES = new HashSet<>(createPrimitives().values());

    private static String toPrimitive(String type) {
        String s = TYPE_PRIMITIVES.get(type);
        if (s != null) {
            return s;
        } else {
            return type;
        }
    }

    private static Map<String, String> createPrimitives() {
        Map<String, String> map = new HashMap<>();
        map.put("Byte", "byte");
        map.put("Short", "short");
        map.put("Integer", "int");
        map.put("Long", "long");
        map.put("Float", "float");
        map.put("Double", "double");
        map.put("Boolean", "boolean");
        List<Entry<String, String>> entries = new ArrayList<>(map.entrySet());
        entries.forEach(entry -> map.put("java.lang." + entry.getKey(), entry.getValue()));
        return map;
    }

    // VisibleForTesting
    static final class Parameter {

        private final String type;
        private final String name;

        public Parameter(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String type() {
            return type;
        }

        public String name() {
            return name;
        }

        boolean isOptional() {
            return type().startsWith("Optional<") || type().startsWith("java.util.Optional<");
        }

        boolean isPrimitive() {
            return PRIMITIVES.contains(type);
        }
    }

    private static boolean isWhitespace(String s) {
        return s != null && s.trim().isEmpty();
    }

    private static String skipWhitespace(String token, StringBuilder b, Iterator<String> it) {
        if (token == null) {
            // EOF
            return null;
        }
        while (isWhitespace(token)) {
            token = next(b, it);
        }
        return token;
    }

    private static String next(StringBuilder b, Iterator<String> it) {
        if (!it.hasNext()) {
            return null;
        }
        String token = it.next();
        b.append(token);
        return skipWhitespace(token, b, it);
    }

}
