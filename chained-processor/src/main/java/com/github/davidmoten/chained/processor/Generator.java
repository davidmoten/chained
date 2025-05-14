package com.github.davidmoten.chained.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.davidmoten.chained.api.Preconditions;

public final class Generator {

    private Generator() {
        // prevent instantiation
    }

    // VisibleForTesting
    static String chainedBuilder(String className, String builderClassName, List<Parameter> parameters,
            boolean constructorVisible, boolean alwaysIncludeBuildMethod) {
        Output o = new Output();
        o.line("package %s;", Util.pkg(builderClassName));
        o.line();
        o.importsHere();
        o.line();
        String builderSimpleClassName = Util.simpleClassName(builderClassName);
        o.line("public final class %s {", builderSimpleClassName);
        o.line();
        List<Parameter> mandatory = parameters.stream().filter(p -> !p.isOptional()).collect(Collectors.toList());
        List<Parameter> optionals = parameters.stream().filter(p -> p.isOptional()).collect(Collectors.toList());
        if (mandatory.isEmpty()) {
            writeSimpleBuilder(o, className, builderSimpleClassName, parameters, constructorVisible);
            return o.toString();
        } else if (optionals.isEmpty() && mandatory.size() == 1) {
            Parameter p = mandatory.get(0);
            o.line("public static %s of(%s %s) {", className, p.type(), p.name());
            // TODO use reflection to call non-visible constructor
            o.line("return new %s(%s);", className, p.name());
            o.close();
            o.close();
            return o.toString();
        } else {
            for (Parameter p : parameters) {
                if (p.isOptional()) {
                    o.line("private %s %s = %s.empty();", p.type(), wrappingType(p.name()), "java.util.Optional");
                } else {
                    o.line("private %s %s;", p.type(), p.name());
                }
            }
            privateConstructor(o, builderSimpleClassName);
            o.line();
            o.line("public static %s builder() {", builderSimpleClassName);
            o.line("return new %s();", builderSimpleClassName);
            o.close();
            o.line();
            o.line("public static %s create() {", builderSimpleClassName);
            o.line("return builder();");
            o.close();
            o.line();
            writeMandatorySetter(o, mandatory.get(0));
            o.line();
            o.line("private %s build() {", className);
            writeBuildStatement(o, className, parameters, constructorVisible);
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
                o.line();

                Parameter q = mandatory.get(i + 1);
                if (i + 1 == mandatory.size() - 1 && optionals.isEmpty()) {
                    if (!alwaysIncludeBuildMethod) {
                        o.line("public %s %s(%s %s) {", className, q.name(), q.type(), q.name());
                        writeNullCheck(o, q);
                        o.line("this._b.%s = %s;", q.name(), q.name());
                        o.line("return _b.build();");
                        o.close();
                    } else {
                        o.line("public %s %s(%s %s) {", builder, q.name(), q.type(), q.name());
                        writeNullCheck(o, q);
                        o.line("this._b.%s = %s;", q.name(), q.name());
                        o.line("return this;");
                        o.close();
                        o.line();
                        o.line("public %s build() {", className);
                        o.line("return _b.build();");
                        o.close();
                    }
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
            o.line("private final %s _b;", builderSimpleClassName);
            o.line();
            o.line("private %s(%s _b) {", lastBuilder, builderSimpleClassName);
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
        o.line("private %s() {", className);
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
            List<Parameter> parameters, boolean constructorVisible) {
        o.line("public static %s builder() {", builderSimpleClassName);
        o.line("return new %s();", builderSimpleClassName);
        o.close();
        o.line();
        for (Parameter p : parameters) {
            if (p.isOptional()) {
                o.line("private %s %s = %s.empty();", p.type(), p.name(), wrappingType(p.type()));
            } else {
                o.line("private %s %s;", p.type(), p.name());
            }
        }
        privateConstructor(o, builderSimpleClassName);
        for (Parameter p : parameters) {
            if (p.isOptional()) {
                String wrappedType = wrappedType(p.type());
                wrappedType = toPrimitive(wrappedType);
                o.line();
                o.line("public %s %s(%s %s) {", builderSimpleClassName, p.name(), wrappedType, p.name());
                o.line("%s.checkNotNull(%s, \"%s\");", Preconditions.class, p.name(), p.name());
                o.line("this.%s = %s.of(%s);", p.name(), wrappingType(p.type()), p.name());
                o.line("return this;");
                o.close();
            }
            o.line();
            o.line("public %s %s(%s %s) {", builderSimpleClassName, p.name(), p.type(), p.name());
            o.line("%s.checkNotNull(%s, \"%s\");", Preconditions.class, p.name(), p.name());
            o.line("this.%s = %s;", p.name(), p.name());
            o.line("return this;");
            o.close();
        }
        o.line();
        o.line("public %s build() {", className);
        writeBuildStatement(o, className, parameters, constructorVisible);
        o.close();
        o.close();
    }

    private static void writeBuildStatement(Output o, String className, List<Parameter> parameters,
            boolean constructorVisible) {
        String params = parameters.stream().map(x -> x.name()).collect(Collectors.joining(", "));
        if (constructorVisible) {
            o.line("return new %s(%s);", className, params);
        } else {
            String parameterClassNames = parameters.stream().map(x -> baseType(x.type()) + ".class")
                    .collect(Collectors.joining(", "));
            o.line("// use reflection to call non-visible constructor");
            o.line("try {");
            o.line("java.lang.reflect.Constructor<%s> _c = %s.class.getDeclaredConstructor(%s);", className, className,
                    parameterClassNames);
            o.line("_c.setAccessible(true);");
            o.line("return _c.newInstance(%s);", params);
            o.close();
            o.line("catch (java.lang.reflect.InvocationTargetException");
            o.right().right();
            o.line("| NoSuchMethodException");
            o.line("| InstantiationException");
            o.line("| IllegalAccessException e) {");
            o.left().left();
            o.line("throw new RuntimeException(e);");
            o.close();
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
        private StringBuilder b = new StringBuilder();
        private Imports imports = new Imports();

        private String indent = "";

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
            b.append(String.format("\n" + indent + fmt, args2));
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
            String s = b.toString();
            return s.replace(IMPORTS_HERE + "\n", imports.toCode());
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
}
