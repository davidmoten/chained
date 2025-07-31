package com.github.davidmoten.chained.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.github.davidmoten.chained.api.Helpers;
import com.github.davidmoten.chained.api.ListBuilder;
import com.github.davidmoten.chained.api.MapBuilder;
import com.github.davidmoten.chained.api.Preconditions;
import com.github.davidmoten.chained.api.SetBuilder;

import jakarta.annotation.Generated;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class Generator {

    private Generator() {
        // prevent instantiation
    }

    public enum Construction {
        DIRECT, REFLECTION, INTERFACE_IMPLEMENTATION
    }

    private enum CollectionType {
        LIST, SET, MAP
    }

    private static final Map<String, String> COLLECTION_IMPLEMENTATION_TYPES = createCollectionImplementationTypes();
    private static final Map<String, CollectionType> COLLECTION_TYPES = createCollectionTypes();

    // VisibleForTesting
    static String chainedBuilder(String className, String builderClassName, List<Parameter> parameters,
            Construction construction, boolean alwaysIncludeBuildMethod, String implementationClassName,
            boolean includeCopyMethod) {
        Output o = new Output(builderClassName);
        o.generatedComment();
        o.line("package %s;", Util.pkg(builderClassName));
        o.importsHere();
        o.line();
        String builderSimpleClassName = Util.simpleClassName(builderClassName);
        o.line("@%s(\"%s\")", Generated.class, "com.github.davidmoten:chained-processor");
        o.line("public final class %s {", builderSimpleClassName);
        o.line();
        List<Parameter> mandatory = parameters //
                .stream() //
                .filter(p -> !p.isOptional() && !p.isNullable()) //
                .collect(Collectors.toList());
        List<Parameter> optionalOrNullable = parameters //
                .stream() //
                .filter(p -> p.isOptional() || p.isNullable()) //
                .collect(Collectors.toList());
        if (mandatory.isEmpty()) {
            writeSimpleBuilder(o, className, builderSimpleClassName, parameters, construction, implementationClassName,
                    includeCopyMethod);
            return o.toString();
        } else if (optionalOrNullable.isEmpty() && mandatory.size() == 1) {
            Parameter p = mandatory.get(0);
            o.line("public static %s of(%s %s %s) {", o.add(className), ann(o, p), o.add(p.type()), p.name());
            writeBuildStatement(o, className, builderSimpleClassName, parameters, construction,
                    implementationClassName);
            o.close();
            o.close();
            return o.toString();
        } else {
            for (Parameter p : parameters) {
                if (p.isOptional()) {
                    o.line("private %s %s = %s.empty();", o.add(p.type()), o.add(outerType(p.name())), Optional.class);
                } else {
                    o.line("private %s %s;", o.add(p.type()), p.name());
                }
            }
        }
        privateConstructor(o, builderSimpleClassName);
        writeStaticCreators(o, builderSimpleClassName, construction);
        o.line();
        writeMandatorySetter(o, mandatory.get(0));
        o.line();
        o.line("private %s build() {", o.add(className));
        writeBuildStatement(o, className, builderSimpleClassName, parameters, construction, implementationClassName);
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
            if (i + 1 == mandatory.size() - 1 && optionalOrNullable.isEmpty()) {
                if (!alwaysIncludeBuildMethod) {
                    writeBuilderForCollection(o, q, o.add(className), "_b.", "_b.build()");
                    o.line();
                    o.line("public %s %s(%s %s %s) {", o.add(className), q.name(), ann(o, p), o.add(q.type()),
                            q.name());
                    writeNullCheck(o, q);
                    assignBuilderField(o, q);
                } else {
                    writeBuilderForCollection(o, q, builder, "_b.", "this");
                    o.line();
                    o.line("public %s %s(%s %s %s) {", builder, q.name(), ann(o, p), o.add(q.type()), q.name());
                    writeNullCheck(o, q);
                    assignBuilderField(o, q);
                    o.line("return this;");
                    o.close();
                    o.line();
                    o.line("public %s build() {", o.add(className));
                }
                o.line("return _b.build();");
                o.close();
                o.close();
                writeCopyBuilder(className, parameters, construction, implementationClassName, includeCopyMethod, o);
                o.close();
                return o.toString();
            } else {
                String nextBuilder = builderClassName(q.name());
                o.line();
                o.line("public %s %s(%s %s %s) {", nextBuilder, q.name(), ann(o, p), o.add(q.type()), q.name());
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
        for (Parameter p : optionalOrNullable) {
            if (p.isOptional()) {
                o.line();
                writeOptionalFieldJavadoc(p, o);
                o.line("public %s %s(@%s %s %s) {", lastBuilder, p.name(), Nonnull.class,
                        o.add(toPrimitive(innerType(p.type()))), p.name());
                writeNullCheck(o, p);
                o.line("this._b.%s = %s.of(%s);", p.name(), o.add(outerType(p.type())), p.name());
                o.line("return this;");
                o.close();
            }
            o.line();
            if (p.isOptional()) {
                writeOptionalFieldOverloadJavadoc(p, o);
            }
            o.line("public %s %s(%s %s %s) {", lastBuilder, p.name(), ann(o, p), o.add(p.type()), p.name());
            if (!p.isNullable()) {
                writeNullCheck(o, p);
            }
            assignBuilderField(o, p);
            o.line("return this;");
            o.close();
        }
        o.line();
        o.line("public %s build() {", o.add(className));
        o.line("return _b.build();");
        o.close();
        o.close();
        writeCopyBuilder(className, parameters, construction, implementationClassName, includeCopyMethod, o);
        o.close();
        return o.toString();
    }

    private static void writeCopyBuilder(String className, List<Parameter> parameters, Construction construction,
            String implementationClassName, boolean includeCopyMethod, Output o) {
        if (!includeCopyMethod || construction == Construction.INTERFACE_IMPLEMENTATION) {
            return;
        }
        o.line();
        o.line("public static CopyBuilder copy(@%s %s value) {", Nonnull.class, o.add(className));
        o.line("return new CopyBuilder(value);");
        o.close();
        o.line();
        o.line("public static final class CopyBuilder {");
        o.line();
        for (Parameter p : parameters) {
            o.line("private %s %s;", o.add(p.type()), p.name());
        }
        o.line();
        o.line("private CopyBuilder(%s value) {", o.add(className));
        for (Parameter p : parameters) {
            o.line("this.%s = value.%s();", p.name(), p.name());
        }
        o.close();
        for (Parameter p : parameters) {
            if (p.isOptional()) {
                o.line();
                writeOptionalFieldJavadoc(p, o);
                o.line("public CopyBuilder %s(%s %s %s) {", p.name(), ann(o, p), o.add(innerType(p.type())), p.name());
                o.line("this.%s = %s.of(%s);", p.name(), Optional.class, p.name());
                o.line("return this;");
                o.close();
            }
            o.line();
            if (p.isOptional()) {
                writeOptionalFieldOverloadJavadoc(p, o);
            }
            o.line("public CopyBuilder %s(%s %s %s) {", p.name(), ann(o, p), o.add(p.type()), p.name());
            o.line("this.%s = %s;", p.name(), p.name());
            o.line("return this;");
            o.close();
        }
        o.line();
        o.line("public %s build() {", o.add(className));
        String args = parameters.stream() //
                .map(p -> "this." + p.name()) //
                .collect(Collectors.joining(", "));
        if (construction == Construction.REFLECTION) {
            o.line("return create(%s);", args);
        } else {
            o.line("return new %s(%s);", o.add(className), args);
        }
        o.close();
        if (construction == Construction.REFLECTION) {
            o.line();
            o.line("private static %s create(Object... args) {", o.add(className));
            writeBuildStatement(o, className, parameters, construction, implementationClassName, "args");
            o.close();
        }
        o.close();
    }

    private static Map<String, CollectionType> createCollectionTypes() {
        Map<String, CollectionType> m = new HashMap<>();
        m.put(List.class.getCanonicalName(), CollectionType.LIST);
        m.put(LinkedList.class.getCanonicalName(), CollectionType.LIST);
        m.put(ArrayList.class.getCanonicalName(), CollectionType.LIST);
        m.put(Map.class.getCanonicalName(), CollectionType.MAP);
        m.put(LinkedHashMap.class.getCanonicalName(), CollectionType.MAP);
        m.put(SortedMap.class.getCanonicalName(), CollectionType.MAP);
        m.put(NavigableMap.class.getCanonicalName(), CollectionType.MAP);
        m.put(TreeMap.class.getCanonicalName(), CollectionType.MAP);
        m.put(Set.class.getCanonicalName(), CollectionType.SET);
        m.put(SortedSet.class.getCanonicalName(), CollectionType.SET);
        m.put(TreeSet.class.getCanonicalName(), CollectionType.SET);
        return m;
    }

    private static Optional<String> collectionImplementationType(Parameter p) {
        TypeModel tm = typeModel(p.type());
        return Optional.ofNullable(COLLECTION_IMPLEMENTATION_TYPES.get(tm.baseType));
    }

    private static Map<String, String> createCollectionImplementationTypes() {
        Map<String, String> m = new HashMap<>();
        add(m, List.class, ArrayList.class);
        add(m, LinkedList.class, LinkedList.class);
        add(m, ArrayList.class, ArrayList.class);
        add(m, Map.class, LinkedHashMap.class);
        add(m, LinkedHashMap.class, LinkedHashMap.class);
        add(m, SortedMap.class, TreeMap.class);
        add(m, NavigableMap.class, TreeMap.class);
        add(m, TreeMap.class, TreeMap.class);
        add(m, Set.class, HashSet.class);
        add(m, SortedSet.class, TreeSet.class);
        add(m, TreeSet.class, TreeSet.class);
        add(m, LinkedHashSet.class, LinkedHashSet.class);
        return m;
    }

    private static void add(Map<String, String> map, Class<?> a, Class<?> b) {
        map.put(a.getCanonicalName(), b.getCanonicalName());
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

    private static String innerType(String type) {
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

    private static String outerType(String type) {
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
        o.line("public %s %s(%s %s %s) {", nextBuilder, p.name(), ann(o, p), o.add(p.type()), p.name());
        if (!p.isNullable()) {
            writeNullCheck(o, p);
        }
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
            List<Parameter> parameters, Construction construction, String implementationClassName,
            boolean includeCopyMethod) {
        for (Parameter p : parameters) {
            if (p.isOptional()) {
                o.line("private %s %s = %s.empty();", o.add(p.type()), p.name(), o.add(outerType(p.type())));
            } else {
                o.line("private %s %s;", o.add(p.type()), p.name());
            }
        }
        privateConstructor(o, builderSimpleClassName);

        writeStaticCreators(o, builderSimpleClassName, construction);

        for (Parameter p : parameters) {
            if (p.isOptional()) {
                String wrappedType = innerType(p.type());
                wrappedType = toPrimitive(wrappedType);
                o.line();
                writeOptionalFieldJavadoc(p, o);
                o.line("public %s %s(%s %s %s) {", builderSimpleClassName, p.name(), ann(o, p), o.add(wrappedType),
                        p.name());
                writeNullCheck(o, p);
                o.line("this.%s = %s.of(%s);", p.name(), o.add(outerType(p.type())), p.name());
                o.line("return this;");
                o.close();
            }
            writeBuilderForCollection(o, p, builderSimpleClassName, "this", "this");
            o.line();
            if (p.isOptional()) {
                writeOptionalFieldOverloadJavadoc(p, o);
            }
            o.line("public %s %s(%s %s %s) {", builderSimpleClassName, p.name(), ann(o, p), o.add(p.type()), p.name());
            if (!p.isNullable()) {
                writeNullCheck(o, p);
            }
            assignField(o, p);
            o.line("return this;");
            o.close();
        }
        o.line();
        o.line("public %s build() {", o.add(className));
        writeBuildStatement(o, className, builderSimpleClassName, parameters, construction, implementationClassName);
        o.close();
        writeCopyBuilder(className, parameters, construction, implementationClassName, includeCopyMethod, o);
        o.close();
    }

    private static void writeOptionalFieldJavadoc(Parameter p, Output o) {
        writeOptionalFieldJavadoc(p, o, false);
    }

    private static void writeOptionalFieldOverloadJavadoc(Parameter p, Output o) {
        writeOptionalFieldJavadoc(p, o, true);
    }

    private static void writeOptionalFieldJavadoc(Parameter p, Output o, boolean isOptionalOverload) {
        String text;
        if (isOptionalOverload) {
            text = String.format(
                    "Sets %s. This parameter is <b>OPTIONAL</b>, the call can be omitted or this method can be called with {@code Optional.empty()}.",
                    p.name());
        } else {
            text = String.format(
                    "Sets %s. This parameter is <b>OPTIONAL</b>, the call can be omitted or an overload can be called with {@code Optional.empty()}.",
                    p.name());
        }
        final int maxLength = 80;
        List<String> lines = new ArrayList<>();
        while (text.length() > maxLength) {
            char ch = text.charAt(maxLength);
            if (Character.isWhitespace(ch)) {
                lines.add(text.substring(0, maxLength));
                text = text.substring(maxLength).trim();
            } else {
                for (int i = maxLength - 1; i >= 0; i--) {
                    ch = text.charAt(i);
                    if (Character.isWhitespace(ch)) {
                        lines.add(text.substring(0, i));
                        text = text.substring(i).trim();
                        break;
                    }
                }
            }
        }
        if (text.length() > 0) {
            lines.add(text);
        }
        o.line("/**");
        lines.stream() //
                .map(line -> " * " + line) //
                .forEach(o::line);
        o.line(" *");
        o.line(" * @param %s the value to set", p.name());
        o.line(" * @return this builder");
        o.line(" */");
    }

    public static String ann(Output o, Parameter p) {
        return "@" + (p.isNullable() ? o.add(Nullable.class) : o.add(Nonnull.class));
    }

    private static void writeBuilderForCollection(Output o, Parameter p, String builderSimpleClassName,
            String fieldPrefix, String returnExpression) {
        writeBuilderForMap(o, p, builderSimpleClassName, fieldPrefix, returnExpression);
        writeBuilderForList(o, p, builderSimpleClassName, fieldPrefix, returnExpression);
        writeBuilderForSet(o, p, builderSimpleClassName, fieldPrefix, returnExpression);
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
            o.line("%s%s = %s%s == null ? new %s<>() : %s%s;", fieldPrefix, p.name(), fieldPrefix, p.name(),
                    collectionImplementationType(p).orElse(LinkedHashMap.class.getCanonicalName()), fieldPrefix,
                    p.name());
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
            o.line("%s%s = %s%s == null ? new %s<>() : %s%s;", fieldPrefix, p.name(), fieldPrefix, p.name(),
                    collectionImplementationType(p).orElse(ArrayList.class.getCanonicalName()), fieldPrefix, p.name());
            o.line("return new %s<>(() -> %s, %s%s);", ListBuilder.class, returnExpression, fieldPrefix, p.name());
            o.close();
        }
    }

    private static void writeBuilderForSet(Output o, Parameter p, String builderSimpleClassName, String fieldPrefix,
            String returnExpression) {
        TypeModel tm = typeModel(p.type());
        if (tm.baseType.equals("java.util.Set") && tm.typeArguments.size() == 1) {
            o.line();
            String genericType = tm.typeArguments.get(0).render();
            o.line("public %s<%s, %s> %s() {", SetBuilder.class, o.add(genericType), builderSimpleClassName, p.name());
            o.line("%s%s = %s%s == null ? new %s<>() : %s%s;", fieldPrefix, p.name(), fieldPrefix, p.name(),
                    collectionImplementationType(p).orElse(HashSet.class.getCanonicalName()), fieldPrefix, p.name());
            o.line("return new %s<>(() -> %s, %s%s);", SetBuilder.class, returnExpression, fieldPrefix, p.name());
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

        public boolean isMap() {
            return baseType.contains("Map");
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
        String outerType = outerType(p.type());
        CollectionType collectionType = COLLECTION_TYPES.get(outerType);
        if (collectionType == CollectionType.MAP) {
            setCollectionField(o, p, variable, LinkedHashMap.class);
        } else if (collectionType == CollectionType.LIST) {
            setCollectionField(o, p, variable, ArrayList.class);
        } else if (collectionType == CollectionType.SET) {
            setCollectionField(o, p, variable, HashSet.class);
        } else {
            o.line("%s.%s = %s;", variable, p.name(), p.name());
        }
    }

    private static void setCollectionField(Output o, Parameter p, String variable, Class<?> collectionImplementation) {
        o.line("%s.%s = %s.addToCollection(", //
                variable, //
                p.name(), //
                Helpers.class);
        o.right().right().right();
        o.line("%s.%s, %s, () -> new %s<>(), %s);", //
                variable, //
                p.name(), //
                p.name(), o.add(collectionImplementationType(p) //
                        .orElse(collectionImplementation.getCanonicalName())), //
                p.isNullable());
        o.left().left().left();
    }

    private static void assignField(Output o, Parameter p) {
        assignField(o, p, "this");
    }

    private static void assignBuilderField(Output o, Parameter p) {
        assignField(o, p, "_b");
    }

    private static void writeBuildStatement(Output o, String className, String builderSimpleClassName,
            List<Parameter> parameters, Construction construction, String implementationClassName) {
        String args = parameters.stream()
                .map(x -> String.format("\n%s%s.unmodifiable(%s)", repeat("    ", 4), o.add(Helpers.class), x.name())) //
                .collect(Collectors.joining(","));
        writeBuildStatement(o, className, parameters, construction, implementationClassName, args);
    }

    private static void writeBuildStatement(Output o, String className, List<Parameter> parameters,
            Construction construction, String implementationClassName, String args) {
        if (construction == Construction.DIRECT) {
            o.line("return new %s(%s);", o.add(className), args);
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
            o.line("return _c.newInstance(%s);", args);
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
            o.line("return %s.create(%s);", o.add(implementationClassName), args);
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
            try {
                indent = indent.substring(0, indent.length() - 4);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalStateException(
                        "cannot left indent when indent is empty, output contents so far:\n" + toString(), e);
            }
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

        public String add(Class<?> cls) {
            return imports.add(cls);
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
        private final boolean nullable;

        Parameter(String type, String name, boolean nullable) {
            this.type = type;
            this.name = name;
            this.nullable = nullable;
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

        boolean isNullable() {
            return nullable;
        }

        @Override
        public String toString() {
            return "Parameter [type=" + type + ", name=" + name + "]";
        }
    }

    static String generateImplemetationClass(String className, List<Parameter> parameters,
            String implementationClassName, Optional<String> checkMethodName) {
        Output o = new Output(implementationClassName);
        String implementationSimpleClassName = Util.simpleClassName(implementationClassName);
        o.generatedComment();
        o.line("package %s;", Util.pkg(implementationClassName));
        o.importsHere();
        o.line();
        o.line("public class %s implements %s {", implementationSimpleClassName, o.add(className));
        o.line();
        for (Parameter p : parameters) {
            o.line("private final %s %s;", o.add(p.type()), p.name());
        }
        o.line();
        o.line("private %s(%s) {", implementationSimpleClassName, asArguments(parameters, o));
        for (Parameter p : parameters) {
            if (!p.isPrimitive()) {
                o.line("%s.checkNotNull(%s, \"%s\");", Preconditions.class, p.name(), p.name());
            }
        }
        for (Parameter p : parameters) {
            o.line("this.%s = %s;", p.name(), p.name());
        }
        checkMethodName.ifPresent(s -> o.line("%s();", s));
        o.close();
        o.line();
        o.line("public static %s create(%s) {", implementationSimpleClassName, asArguments(parameters, o));
        if (checkMethodName.isPresent()) {
            o.line("%s _o = new %s(%s);", implementationSimpleClassName, implementationSimpleClassName,
                    parameters.stream().map(x -> x.name()).collect(Collectors.joining(", ")));
            o.line("_o.%s();", checkMethodName.get());
            o.line("return _o;");
        } else {
            o.line("return new %s(%s);", implementationSimpleClassName,
                    parameters.stream().map(x -> x.name()).collect(Collectors.joining(", ")));
        }
        o.close();
        for (Parameter p : parameters) {
            o.line();
            o.line("@%s", Override.class);
            o.line(ann(o, p));
            o.line("public %s %s() {", o.add(p.type()), p.name());
            o.line("return %s;", p.name());
            o.close();
        }
        writeToString(parameters, o, implementationSimpleClassName);
        writeEquals(parameters, o, implementationSimpleClassName);
        writeHashCode(parameters, o);
        return o.toString();
    }

    private static void writeToString(List<Parameter> parameters, Output o, String implementationSimpleClassName) {
        o.line();
        o.line("@%s", Override.class);
        o.line("public String toString() {");
        o.line("%s b = new %s();", StringBuilder.class, StringBuilder.class);
        o.line("b.append(\"%s[\");", implementationSimpleClassName);
        boolean first = true;
        for (Parameter p : parameters) {
            String extra = first ? "" : ", ";
            o.line("b.append(\"%s%s=\");", extra, p.name());
            o.line("b.append(%s.valueOf(this.%s));", String.class, p.name());
            first = false;
        }
        o.line("b.append(\"]\");");
        o.line("return b.toString();");
        o.close();
    }

    private static void writeEquals(List<Parameter> parameters, Output o, String implementationSimpleClassName) {
        o.line();
        o.line("@%s", Override.class);
        o.line("public boolean equals(%s o) {", Object.class);
        o.line("if (this == o) return true;");
        o.line("if (o == null) return false;");
        o.line("if (getClass() != o.getClass()) return false;");
        o.line("%s other = (%s) o;", implementationSimpleClassName, implementationSimpleClassName);
        if (parameters.isEmpty()) {
            o.line("return true;");
        } else {
            o.line("return");
            o.right();
            for (int i = 0; i < parameters.size(); i++) {
                Parameter p = parameters.get(i);
                String prefix = i == 0 ? "" : "&& ";
                String suffix = i == parameters.size() - 1 ? ";" : "";
                o.line("%s%s.equals(this.%s, other.%s)%s", prefix, Objects.class, p.name, p.name, suffix);
            }
            o.left();
        }
        o.close();
    }

    private static void writeHashCode(List<Parameter> parameters, Output o) {
        o.line();
        o.line("@%s", Override.class);
        o.line("public int hashCode() {");
        String args = parameters.stream().map(x -> x.name()).collect(Collectors.joining(", "));
        o.line("return %s.hash(%s);", Objects.class, args);
        o.close();
        o.close();
    }
}
