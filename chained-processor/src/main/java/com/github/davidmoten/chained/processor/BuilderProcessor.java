package com.github.davidmoten.chained.processor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.api.annotation.BuilderConstructor;
import com.github.davidmoten.chained.api.annotation.Check;
import com.github.davidmoten.chained.processor.Generator.Construction;
import com.github.davidmoten.chained.processor.Generator.Parameter;

import jakarta.annotation.Nullable;

@SupportedAnnotationTypes("com.github.davidmoten.chained.api.annotation.Builder")
public final class BuilderProcessor extends AbstractProcessor {

    private static final String DEFAULT_BUILDER_CLASS_NAME_TEMPLATE = "${pkg}.builder.${simpleName}Builder";
    private static final String DEFAULT_IMPLEMENTATION_CLASS_NAME_TEMPLATE = "${pkg}.builder.${simpleName}Impl";
    private static final String DEFAULT_JAVADOCS_LOCATION = "src/main/javadoc";
    private Elements utils;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        utils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Javadocs javadocs = new Javadocs(processingEnv);
        try {
            for (Element element : roundEnv.getElementsAnnotatedWith(Builder.class)) {
                if (element instanceof TypeElement) {
                    TypeElement typeElement = (TypeElement) element;
                    log(Kind.NOTE, "processing Builder annotation: " //
                            + typeElement + " " + typeElement.getKind() + " " + typeElement.getNestingKind() + " "
                            + typeElement.getModifiers());
                    if (typeElement.getNestingKind() == NestingKind.MEMBER
                            && !typeElement.getModifiers().contains(Modifier.STATIC)) {
                        log(Kind.WARNING, "nested classes must be static to use @Builder", element);
                        continue;
                    }
                    String packageName = processingEnv //
                            .getElementUtils() //
                            .getPackageOf(typeElement) //
                            .getQualifiedName().toString();
                    String simpleClassName = typeElement.getSimpleName().toString();
                    String fullClassName = packageName.isEmpty() ? simpleClassName
                            : packageName + "." + simpleClassName;

                    String defaultBuilderClassName = processingEnv //
                            .getOptions() //
                            .getOrDefault("generatedClassName", DEFAULT_BUILDER_CLASS_NAME_TEMPLATE);
                    Builder annotation = typeElement.getAnnotation(Builder.class);
                    String templatedBuilderClassName = isEmpty(annotation.value()) ? defaultBuilderClassName
                            : annotation.value();

                    String builderClassName = templatedBuilderClassName //
                            .replace("${pkg}", packageName) //
                            .replace("${simpleName}", simpleClassName);
                    String defaultImplementationClassName = processingEnv //
                            .getOptions() //
                            .getOrDefault("generatedImplementationClassName",
                                    DEFAULT_IMPLEMENTATION_CLASS_NAME_TEMPLATE);
                    String templatedImplementationClassName = isEmpty(annotation.implementationClassName())
                            ? defaultImplementationClassName
                            : annotation.implementationClassName();
                    String implementationClassName = templatedImplementationClassName //
                            .replace("${pkg}", packageName) //
                            .replace("${simpleName}", simpleClassName);
                    try {
                        Filer filer = processingEnv.getFiler();
                        {
                            JavaFileObject file = filer.createSourceFile(builderClassName);
                            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                                if (typeElement.getKind() == ElementKind.INTERFACE) {
                                    generateFromInterface(typeElement, packageName, annotation, builderClassName,
                                            implementationClassName, javadocs, out);
                                } else if (typeElement.getKind() == ElementKind.CLASS
                                        || typeElement.getKind().name().equals("RECORD")) {
                                    generateFromClassOrRecord(typeElement, packageName, annotation, builderClassName,
                                            implementationClassName, javadocs, fullClassName, out);
                                } else {
                                    log(Kind.WARNING, "class type " + typeElement.getKind()
                                            + " not supported for builder generation");
                                }
                            }
                        }
                        if (typeElement.getKind() == ElementKind.INTERFACE) {
                            JavaFileObject file = filer.createSourceFile(implementationClassName);
                            try (PrintWriter out = new PrintWriter(file.openWriter())) {
                                if (typeElement.getKind() == ElementKind.INTERFACE) {
                                    String className = typeElement.getQualifiedName().toString();
                                    List<Parameter> parameters = parametersFromInterface(typeElement,
                                            implementationClassName, javadocs);
                                    String code = Generator.generateImplementationClass(className, parameters,
                                            implementationClassName, checkMethodName(typeElement));
                                    out.println(code);
                                }
                            }
                        }

                    } catch (IOException | RuntimeException e) {
                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        try (PrintWriter writer = new PrintWriter(b)) {
                            e.printStackTrace(writer);
                        }
                        log(Kind.ERROR, new String(b.toByteArray(), StandardCharsets.UTF_8), element);
                        return false;
                    }
                }
            }
            return true;
        } catch (RuntimeException e) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try (PrintWriter writer = new PrintWriter(b)) {
                e.printStackTrace(writer);
            }
            log(Kind.ERROR, new String(b.toByteArray(), StandardCharsets.UTF_8));
            return false;
        }
    }

    public static final class Javadocs {
        private static final String JAVADOC_FILE_EXTENSION = ".txt";
        private final Map<String, String> map;

        Javadocs(ProcessingEnvironment processingEnv) {
            String javadocsLocation = processingEnv //
                    .getOptions() //
                    .getOrDefault("javadocDir", DEFAULT_JAVADOCS_LOCATION);
            File javadocsDir = new File(javadocsLocation);
            File[] files = javadocsDir.listFiles();
            if (files != null) {
                map = Arrays.stream(files) //
                        .filter(file -> file.isDirectory()) //
                        .flatMap(file -> Arrays.stream(file.listFiles())
                                .filter(f -> f.isFile() && f.getName().endsWith(JAVADOC_FILE_EXTENSION)) //
                                .map(f -> toPair(file, f)))
                        .collect(Collectors.toMap(Pair::key, Pair::value));
            } else {
                map = Collections.emptyMap();
            }
        }

        public Optional<String> get(String fullClassName, String fieldName) {
            String key = fullClassName + ":" + fieldName;
            return Optional.ofNullable(map.get(key));
        }

        private static Pair toPair(File file, File f) {
            String name = f.getName().substring(0, f.getName().length() - JAVADOC_FILE_EXTENSION.length());
            try {
                String text = new String(java.nio.file.Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                return new Pair(file.getName() + ":" + name, text);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private static final class Pair {
            final String key;
            final String value;

            Pair(String key, String value) {
                this.key = key;
                this.value = value;
            }

            String key() {
                return key;
            }

            String value() {
                return value;
            }
        }

        @Override
        public String toString() {
            return "Javadocs [map=" + map + "]";
        }
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void log(Kind kind, String message, Element element) {
        processingEnv.getMessager().printMessage(kind, message, element);
    }

    private void log(Kind kind, String message) {
        processingEnv.getMessager().printMessage(kind, message);
    }

    private static Optional<String> checkMethodName(TypeElement typeElement) {
        List<ExecutableElement> list = typeElement //
                .getEnclosedElements() //
                .stream() //
                .filter(x -> x.getKind() == ElementKind.METHOD) //
                .filter(x -> !x.getModifiers().contains(Modifier.STATIC)) //
                .map(x -> (ExecutableElement) x) //
                .filter(x -> x.getParameters().isEmpty()) //
                .filter(x -> x.getAnnotation(Check.class) != null) //
                .collect(Collectors.toList());
        if (list.size() > 1) {
            throw new IllegalStateException(
                    "interface " + typeElement.getSimpleName() + " can only have @Check method");
        } else if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(0).getSimpleName().toString());
        }
    }

    private void generateFromInterface(TypeElement typeElement, String packageName, Builder annotation,
            String builderClassName, String implementationClassName, Javadocs javadocs, PrintWriter out) {
        List<Parameter> parameters = parametersFromInterface(typeElement, implementationClassName, javadocs);
        out.print(Generator.chainedBuilder( //
                typeElement.getQualifiedName().toString(), //
                builderClassName, //
                parameters, //
                Construction.INTERFACE_IMPLEMENTATION, //
                annotation.alwaysIncludeBuildMethod(), //
                implementationClassName, //
                annotation.copy()));
        out.println();
    }

    private static List<Parameter> parametersFromInterface(TypeElement typeElement, String implementationClassName,
            Javadocs javadocs) {
        return typeElement //
                .getEnclosedElements() //
                .stream() //
                .filter(x -> x.getKind() == ElementKind.METHOD) //
                .filter(x -> !x.getModifiers().contains(Modifier.STATIC)
                        && !x.getModifiers().contains(Modifier.DEFAULT)) //
                .map(x -> (ExecutableElement) x) //
                .filter(x -> x.getParameters().isEmpty()) //
                .map(x -> new Parameter(x.getReturnType().toString(), x.getSimpleName().toString(),
                        x.getAnnotation(Nullable.class) != null,
                        javadocs.get(implementationClassName, x.getSimpleName().toString()), true)) //
                .collect(Collectors.toList());
    }

    private void generateFromClassOrRecord(TypeElement typeElement, String packageName, Builder annotation,
            String builderClassName, String implementationClassName, Javadocs javadocs, String fullClassName,
            PrintWriter out) {
        String builderPackageName = Util.pkg(builderClassName);
        ExecutableElement constructor = constructor(typeElement);
        Map<String, String> fieldJavadoc = fieldJavadoc(typeElement, utils);
        List<Parameter> parameters = constructor //
                .getParameters() //
                .stream() //
                .map(p -> {
                    Optional<String> prerenderedJavadoc = javadocs.get(fullClassName, p.getSimpleName().toString());
                    return new Parameter( //
                            p.asType().toString(), //
                            p.getSimpleName().toString(), //
                            p.getAnnotation(Nullable.class) != null, //
                            Optional.ofNullable(prerenderedJavadoc //
                                    .orElse(fieldJavadoc.get(p.getSimpleName().toString()))),
                            prerenderedJavadoc.isPresent());
                }) //
                .collect(Collectors.toList());
        Set<Modifier> modifiers = constructor.getModifiers();
        boolean constructorVisible = //
                modifiers.contains(Modifier.PUBLIC) //
                        || //
                        !modifiers.contains(Modifier.PRIVATE) //
                                && !modifiers.contains(Modifier.PROTECTED) //
                                && packageName.equals(builderPackageName);
        out.print(Generator.chainedBuilder( //
                typeElement.getQualifiedName().toString(), //
                builderClassName, //
                parameters, //
                constructorVisible ? Construction.DIRECT : Construction.REFLECTION, //
                annotation.alwaysIncludeBuildMethod(), implementationClassName, annotation.copy()));
        out.println();
    }

    private static Map<String, String> fieldJavadoc(TypeElement typeElement, Elements utils) {
        Map<String, String> map = new HashMap<>();
        ExecutableElement constructor = constructor(typeElement);
        String text = utils.getDocComment(constructor);
        if (text == null || !text.contains("@param ")) {
            text = utils.getDocComment(typeElement);
        }
        if (text != null) {
            while (text.contains("@param ")) {
                int start = text.indexOf("@param ");
                if (start == -1) {
                    break;
                }
                int end = text.indexOf("@", start + 1);
                if (end == -1) {
                    end = text.length();
                }
                String parameterText = text.substring(start + 7, end).trim().replace("\n", " ").replaceAll("  +", " ");
                text = text.substring(end);
                int spaceIndex = parameterText.indexOf(' ');
                String parameterName = spaceIndex == -1 ? parameterText : parameterText.substring(0, spaceIndex);
                String parameterDescription = spaceIndex == -1 ? "" : parameterText.substring(spaceIndex + 1).trim();
                if (!parameterDescription.isEmpty()) {
                    map.put(parameterName, parameterDescription);
                }
                map.put(parameterName, parameterDescription);
            }
        }
        if (!map.isEmpty()) {
            System.out.println(map);
        }
        return map;
    }

    private static ExecutableElement constructor(TypeElement element) {
        List<ExecutableElement> list = element.getEnclosedElements().stream()
                .filter(elem -> elem.getKind() == ElementKind.CONSTRUCTOR) //
                .map(elem -> (ExecutableElement) elem) //
                .collect(Collectors.toList());

        List<ExecutableElement> defined = list //
                .stream() //
                .filter(c -> c.getAnnotation(BuilderConstructor.class) != null) //
                .collect(Collectors.toList());
        if (defined.size() > 1) {
            throw new IllegalStateException("Multiple constructors with BuilderConstructor annotation found");
        } else if (defined.size() == 1) {
            return defined.get(0);
        } else {
            ExecutableElement max = list.stream()
                    .max((c1, c2) -> Integer.compare(c1.getParameters().size(), c2.getParameters().size()))
                    .orElseThrow(() -> new IllegalStateException("No public constructor found"));
            if (list.stream().filter(c -> c.getParameters().size() == max.getParameters().size()).count() > 1) {
                throw new IllegalStateException(
                        "Multiple max-length public constructors found, there should be just one with maximum length or one with BuilderConstructor annotation");
            }
            return max;
        }
    }
}