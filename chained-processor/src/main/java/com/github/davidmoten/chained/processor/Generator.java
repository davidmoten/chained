package com.github.davidmoten.chained.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import com.github.davidmoten.chained.api.ListBuilder;
import com.github.davidmoten.chained.api.MapBuilder;
import com.github.davidmoten.chained.api.Preconditions;

public final class Generator {

    private Generator() {
        // prevent instantiation
    }

    public enum Construction {
        DIRECT, REFLECTION, INTERFACE_IMPLEMENTATION;
    }

    // VisibleForTesting
    static String chainedBuilder(String className, String builderClassName, List<Parameter> parameters,
            Construction construction, boolean alwaysIncludeBuildMethod, String implementationClassName) {
        Output o = new Output(builderClassName);
        o.generatedComment();
        o.line("package %s;", Util.pkg(builderClassName));
        o.importsHere();
        o.line();
        String builderSimpleClassName = Util.simpleClassName(builderClassName);
        o.line("@%s(\"%s\")", Generated.class, "com.github.davidmoten:chained-processor");
        o.line("public final class %s {", builderSimpleClassName);
        o.line();
        List<Parameter> mandatory = parameters.stream().filter(p -> !p.isOptional()).collect(Collectors.toList());
        List<Parameter> optionals = parameters.stream().filter(p -> p.isOptional()).collect(Collectors.toList());
        if (mandatory.isEmpty()) {
            writeSimpleBuilder(o, className, builderSimpleClassName, parameters, construction, implementationClassName);
            return o.toString();
        } else if (optionals.isEmpty() && mandatory.size() == 1) {
            Parameter p = mandatory.get(0);
            o.line("public static %s of(%s %s) {", o.add(className), o.add(p.type()), p.name());
            writeBuildStatement(o, className, builderSimpleClassName, parameters, construction,
                    implementationClassName);
            o.close();
            o.close();
            return o.toString();
        } else {
            for (Parameter p : parameters) {
                if (p.isOptional()) {
                    o.line("private %s %s = %s.empty();", o.add(p.type()), o.add(wrappingType(p.name())),
                            Optional.class);
                } else if (p.type().startsWith("java.util.Map<")) {
                    o.line("private %s %s = new %s<>();", o.add(p.type()), p.name(), LinkedHashMap.class);
                } else if (p.type().startsWith("java.util.List<")) {
                    o.line("private %s %s = new %s<>();", o.add(p.type()), p.name(), ArrayList.class);
                } else {
                    o.line("private %s %s;", o.add(p.type()), p.name());
                }
            }
            privateConstructor(o, builderSimpleClassName);
            writeStaticCreators(o, builderSimpleClassName, construction);
            o.line();
            writeMandatorySetter(o, mandatory.get(0));
            o.line();
            o.line("private %s build() {", o.add(className));
            writeBuildStatement(o, className, builderSimpleClassName, parameters, construction,
                    implementationClassName);
            o.close();

            for (int i = 0; i < mandatory.size() - 1; i++) {
                Parameter p = mandatory.get(i);
                String builder = builderClassName(p.name());
                o.line();
                o.line("public final static class %s {", builder);
                o.line();
                o.line("private final %s _b;", builderSimpleClassName);
                o.line();
                o.line("private %s(%s _b) {", builder, builderSimpleClassName);
                o.line("this._b = _b;");
                o.close();

                Parameter q = mandatory.get(i + 1);
                if (i + 1 == mandatory.size() - 1 && optionals.isEmpty()) {
                    if (!alwaysIncludeBuildMethod) {
                        writeBuilderForCollection(o, q, o.add(className), "_b.", "_b.build()");
                        o.line();
                        o.line("public %s %s(%s %s) {", o.add(className), q.name(), o.add(q.type()), q.name());
                        writeNullCheck(o, q);
                        assignBuilderField(o, q);
                        o.line("return _b.build();");
                        o.close();
                    } else {
                        writeBuilderForCollection(o, q, builder, "_b.", "this");
                        o.line();
                        o.line("public %s %s(%s %s) {", builder, q.name(), o.add(q.type()), q.name());
                        writeNullCheck(o, q);
                        assignBuilderField(o, q);
                        o.line("return this;");
                        o.close();
                        o.line();
                        o.line("public %s build() {", o.add(className));
                        o.line("return _b.build();");
                        o.close();
                    }
                    o.close();
                    o.close();
                    return o.toString();
                } else {
                    String nextBuilder = builderClassName(q.name());
                    o.line();
                    o.line("public %s %s(%s %s) {", nextBuilder, q.name(), o.add(q.type()), q.name());
                    writeNullCheck(o, q);
                    assignBuilderField(o, q);
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
            o.line("private final %s _b;", builderSimpleClassName);
            o.line();
            o.line("private %s(%s _b) {", lastBuilder, builderSimpleClassName);
            o.line("this._b = _b;");
            o.close();
            for (Parameter p : optionals) {
                o.line();
                o.line("public %s %s(%s %s) {", lastBuilder, p.name(), o.add(toPrimitive(wrappedType(p.type()))),
                        p.name());
                writeNullCheck(o, p);
                o.line("this._b.%s = %s.of(%s);", p.name(), o.add(wrappingType(p.type())), p.name());
                o.line("return this;");
                o.close();
                o.line();
                o.line("public %s %s(%s %s) {", lastBuilder, p.name(), o.add(p.type()), p.name());
                writeNullCheck(o, p);
                assignBuilderField(o, p);
                o.line("return this;");
                o.close();
            }
            o.line();
            o.line("public %s build() {", o.add(className));
            o.line("return _b.build();");
            o.close();
            o.close();
            o.close();
            return o.toString();
        }
    }

    private static String asArguments(List<Parameter> parameters, Output o) {
        if (parameters.size() > 2) {
            String indent = o.indent() + repeat("    ", 3);
            return parameters.stream() //
                    .map(p -> "\n" + indent + o.add(p.type()) + " " + p.name()) //
                    .collect(Collectors.joining(","));
        } else {
            return parameters //
                    .stream()//
                    .map(p -> o.add(p.type()) + " " + p.name()) //
                    .collect(Collectors.joining(", "));
        }
    }

    private static String repeat(String s, int n) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) {
            b.append(s);
        }
        return b.toString();
    }

    private static void writeStaticCreators(Output o, String builderSimpleClassName, Construction construction) {
        o.line();
        o.line("public static %s builder() {", builderSimpleClassName);
        o.line("return new %s();", builderSimpleClassName);
        o.close();
        if (construction != Construction.INTERFACE_IMPLEMENTATION) {
            o.line();
            o.line("public static %s create() {", builderSimpleClassName);
            o.line("return builder();");
            o.close();
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

    private static void privateConstructor(Output o, String simpleClassName) {
        o.line();
        o.line("private %s() {", simpleClassName);
        o.line("// prevent instantiation");
        o.close();
    }

    private static void writeMandatorySetter(Output o, Parameter p) {
        String nextBuilder = builderClassName(p.name());
        o.line("public %s %s(%s %s) {", nextBuilder, p.name(), o.add(p.type()), p.name());
        writeNullCheck(o, p);
        o.line("this.%s = %s;", p.name(), p.name());
        o.line("return new %s(this);", nextBuilder);
        o.close();
    }

    private static void writeNullCheck(Output o, Parameter p) {
        if (!p.isPrimitive()) {
            o.line("%s.checkNotNull(%s, \"%s\");", Preconditions.class, p.name(), p.name());
        }
    }

    private static String builderClassName(String field) {
        return "BuilderWith" + upperFirst(field);
    }

    private static String upperFirst(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static void writeSimpleBuilder(Output o, String className, String builderSimpleClassName,
            List<Parameter> parameters, Construction construction, String implementationClassName) {
        for (Parameter p : parameters) {
            if (p.isOptional()) {
                o.line("private %s %s = %s.empty();", o.add(p.type()), p.name(), o.add(wrappingType(p.type())));
            } else {
                o.line("private %s %s;", o.add(p.type()), p.name());
            }
        }
        privateConstructor(o, builderSimpleClassName);

        writeStaticCreators(o, builderSimpleClassName, construction);

        for (Parameter p : parameters) {
            if (p.isOptional()) {
                String wrappedType = wrappedType(p.type());
                wrappedType = toPrimitive(wrappedType);
                o.line();
                o.line("public %s %s(%s %s) {", builderSimpleClassName, p.name(), o.add(wrappedType), p.name());
                writeNullCheck(o, p);
                o.line("this.%s = %s.of(%s);", p.name(), o.add(wrappingType(p.type())), p.name());
                o.line("return this;");
                o.close();
            }
            writeBuilderForCollection(o, p, builderSimpleClassName, "this", "this");
            o.line();
            o.line("public %s %s(%s %s) {", builderSimpleClassName, p.name(), o.add(p.type()), p.name());
            writeNullCheck(o, p);
            assignField(o, p);
            o.line("return this;");
            o.close();
        }
        o.line();
        o.line("public %s build() {", o.add(className));
        writeBuildStatement(o, className, builderSimpleClassName, parameters, construction, implementationClassName);
        o.close();
        o.close();
    }

    private static void writeBuilderForCollection(Output o, Parameter p, String builderSimpleClassName,
            String fieldPrefix, String returnExpression) {
        writeBuilderForMap(o, p, builderSimpleClassName, fieldPrefix, returnExpression);
        writeBuilderForList(o, p, builderSimpleClassName, fieldPrefix, returnExpression);
    }

    private static void writeBuilderForMap(Output o, Parameter p, String builderSimpleClassName, String fieldPrefix,
            String returnExpression) {
        TypeModel tm = typeModel(p.type());
        if (tm.baseType.equals("java.util.Map") && tm.typeArguments.size() == 2) {
            o.line();
            String keyType = tm.typeArguments.get(0).render();
            String valueType = tm.typeArguments.get(1).render();
            o.line("public %s<%s, %s, %s> %s() {", MapBuilder.class, o.add(keyType), o.add(valueType),
                    builderSimpleClassName, p.name());
            o.line("return new %s<>(() -> %s, %s%s);", MapBuilder.class, returnExpression, fieldPrefix, p.name());
            o.close();
        }
    }

    private static void writeBuilderForList(Output o, Parameter p, String builderSimpleClassName, String fieldPrefix,
            String returnExpression) {
        TypeModel tm = typeModel(p.type());
        if (tm.baseType.equals("java.util.List") && tm.typeArguments.size() == 1) {
            o.line();
            String genericType = tm.typeArguments.get(0).render();
            o.line("public %s<%s, %s> %s() {", ListBuilder.class, o.add(genericType), builderSimpleClassName, p.name());
            o.line("return new %s<>(() -> %s, %s%s);", ListBuilder.class, returnExpression, fieldPrefix, p.name());
            o.close();
        }
    }

    // VisibleForTesting
    static TypeModel typeModel(String type) {
        int i = type.indexOf("<");
        if (i == -1) {
            return new TypeModel(type, Collections.emptyList());
        } else {
            int j = type.lastIndexOf(">");
            if (j == -1) {
                throw new RuntimeException("unexpected lack of closing > in type name: " + type);
            } else {
                String baseType = type.substring(0, i);
                String args = type.substring(i + 1, j);
                List<TypeModel> list = new ArrayList<>();
                int k = 0;
                int depth = 0;
                for (int r = 0; r < args.length(); r++) {
                    char c = args.charAt(r);
                    if (c == '<') {
                        depth++;
                    } else if (c == '>') {
                        depth--;
                    } else if (c == ',' && depth == 0) {
                        String s = args.substring(k, r);
                        list.add(typeModel(s.trim()));
                        k = r + 1;
                    }
                }
                if (k < args.length()) {
                    list.add(typeModel(args.substring(k).trim()));
                }
                return new TypeModel(baseType, list);
            }
        }
    }

    // VisibleForTesting
    static final class TypeModel {
        final String baseType;
        final List<TypeModel> typeArguments;

        TypeModel(String baseType, List<TypeModel> typeArguments) {
            this.baseType = baseType;
            this.typeArguments = typeArguments;
        }

        public String render() {
            return render(x -> x);
        }

        public String render(UnaryOperator<String> transform) {
            if (typeArguments.isEmpty()) {
                return transform.apply(baseType);
            } else {
                String args = typeArguments.stream() //
                        .map(x -> x.render(transform)) //
                        .collect(Collectors.joining(", "));
                return transform.apply(baseType) + "<" + args + ">";
            }
        }
    }

    private static void assignField(Output o, Parameter p, String variable) {
        if (p.type().startsWith("java.util.Map<")) {
            o.line("%s.%s.putAll(%s);", variable, p.name(), p.name());
        } else {
            o.line("%s.%s = %s;", variable, p.name(), p.name());
        }
    }

    private static void assignField(Output o, Parameter p) {
        assignField(o, p, "this");
    }

    private static void assignBuilderField(Output o, Parameter p) {
        assignField(o, p, "_b");
    }

    private static void writeBuildStatement(Output o, String className, String builderSimpleClassName,
            List<Parameter> parameters, Construction construction, String implementationClassName) {
        String params = parameters.stream().map(x -> x.name()).collect(Collectors.joining(", "));
        if (construction == Construction.DIRECT) {
            o.line("return new %s(%s);", o.add(className), params);
        } else if (construction == Construction.REFLECTION) {
            String parameterClassNames = parameters.stream().map(x -> o.add(baseType(x.type())) + ".class")
                    .collect(Collectors.joining(", "));
            o.line("// use reflection to call non-visible constructor");
            o.line("try {");
            o.line("%s<%s> _c = %s.class", Constructor.class, o.add(className), o.add(className));
            o.right().right();
            o.line(".getDeclaredConstructor(%s);", parameterClassNames);
            o.left().left();
            o.line("_c.setAccessible(true);");
            o.line("return _c.newInstance(%s);", params);
            o.close();
            o.line("catch (%s", InvocationTargetException.class);
            o.right().right();
            o.line("| %s", NoSuchMethodException.class);
            o.line("| %s", InstantiationException.class);
            o.line("| %s e) {", IllegalAccessException.class);
            o.left().left();
            o.line("throw new %s(e);", RuntimeException.class);
            o.close();
        } else if (construction == Construction.INTERFACE_IMPLEMENTATION) {
            o.line("return new %s(%s);", o.add(implementationClassName),
                    parameters.stream().map(x -> x.name()).collect(Collectors.joining(", ")));
        }
    }

    private static String baseType(String type) {
        int i = type.indexOf("<");
        if (i == -1) {
            return type;
        } else {
            return type.substring(0, i);
        }
    }

    public static final class Output {

        private static final String IMPORTS_HERE = "<<IMPORTS_HERE>>";
        private final Imports imports;
        private final StringBuilder b = new StringBuilder();
        private boolean firstLine = true;

        public Output(String ownerClassName) {
            this.imports = new Imports(ownerClassName);
        }

        public void generatedComment() {
            line("// GENERATED FILE - DO NOT EDIT");
        }

        private String indent = "";

        public String indent() {
            return indent;
        }

        public Output left() {
            indent = indent.substring(0, indent.length() - 4);
            return this;
        }

        public void importsHere() {
            line(IMPORTS_HERE);
        }

        public Output right() {
            indent += "    ";
            return this;
        }

        public void line() {
            b.append("\n");
        }

        public String add(String type) {
            return add(typeModel(type));
        }

        private String add(TypeModel tm) {
            return tm.render(x -> imports.add(x));
        }

        public void line(String fmt, Object... args) {
            Object[] args2 = // copy args
                    new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object o = args[i];
                if (o instanceof Class) {
                    o = imports.add((Class<?>) o);
                }
                args2[i] = o;
            }
            b.append(String.format((firstLine ? "" : "\n") + indent + fmt, args2));
            if (fmt.endsWith("{")) {
                right();
            }
            firstLine = false;
        }

        public void close() {
            left();
            line("}");
        }

        @Override
        public String toString() {
            String s = b.toString();
            String code = imports.toCode();
            final String code2;
            if (code.trim().isEmpty()) {
                code2 = "";
            } else {
                code2 = "\n" + imports.toCode();
            }
            return s.replace(IMPORTS_HERE + "\n", code2);
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

        Parameter(String type, String name) {
            this.type = type;
            this.name = name;
        }

        String type() {
            return type;
        }

        String name() {
            return name;
        }

        boolean isOptional() {
            return type().startsWith("Optional<") || type().startsWith("java.util.Optional<");
        }

        boolean isPrimitive() {
            return PRIMITIVES.contains(type);
        }

        @Override
        public String toString() {
            return "Parameter [type=" + type + ", name=" + name + "]";
        }
    }

    public static String generateImplemetationClass(String className, List<Parameter> parameters,
            String implementationClassName) {
        Output o = new Output(implementationClassName);
        o.generatedComment();
        o.line("package %s;", Util.pkg(implementationClassName));
        o.importsHere();
        o.line();
        o.line("public class %s implements %s {", o.add(implementationClassName), o.add(className));
        o.line();
        for (Parameter p : parameters) {
            o.line("private final %s %s;", o.add(p.type()), p.name());
        }
        o.line();
        o.line("public %s(%s) {", o.add(implementationClassName), asArguments(parameters, o));
        for (Parameter p : parameters) {
            if (!p.isPrimitive()) {
                o.line("%s.checkNotNull(%s, \"%s\");", Preconditions.class, p.name(), p.name());
            }
        }
        for (Parameter p : parameters) {
            o.line("this.%s = %s;", p.name(), p.name());
        }
        o.close();
        for (Parameter p : parameters) {
            o.line();
            o.line("public %s %s() {", o.add(p.type()), p.name());
            o.line("return %s;", p.name());
            o.close();
        }
        o.close();
        return o.toString();
    }
}
