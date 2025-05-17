package com.github.davidmoten.chained.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import com.github.davidmoten.chained.api.annotation.Builder;
import com.github.davidmoten.chained.api.annotation.BuilderConstructor;
import com.github.davidmoten.chained.processor.Generator.Construction;
import com.github.davidmoten.chained.processor.Generator.Parameter;

@SupportedAnnotationTypes("com.github.davidmoten.chained.api.annotation.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BuilderProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Builder.class)) {
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                String packageName = processingEnv //
                        .getElementUtils() //
                        .getPackageOf(typeElement) //
                        .getQualifiedName().toString();
                String simpleClassName = typeElement.getSimpleName().toString();
                Builder annotation = typeElement.getAnnotation(Builder.class);
                String templatedBuilderClassName = annotation.value();
                String builderClassName = templatedBuilderClassName //
                        .replace("${pkg}", packageName) //
                        .replace("${simpleName}", simpleClassName);

                try {
                    Filer filer = processingEnv.getFiler();
                    JavaFileObject file = filer.createSourceFile(builderClassName);
                    try (PrintWriter out = new PrintWriter(file.openWriter())) {
                        if (typeElement.getKind() == ElementKind.INTERFACE) {
                            generateFromInterface(typeElement, packageName, annotation, builderClassName, out);
                        } else if (typeElement.getKind() == ElementKind.CLASS
                                || typeElement.getKind().name().equals("RECORD")) {
                            generateFromClassOrRecord(typeElement, packageName, annotation, builderClassName, out);
                        } else {
                            processingEnv.getMessager().printMessage(Kind.WARNING,
                                    "class type " + typeElement.getKind() + " not supported for builder generation");
                        }
                    }
                } catch (IOException | RuntimeException e) {
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    try (PrintWriter writer = new PrintWriter(b)) {
                        e.printStackTrace(writer);
                    }
                    processingEnv //
                            .getMessager() //
                            .printMessage(Kind.ERROR, new String(b.toByteArray(), StandardCharsets.UTF_8), element);
                    return false;
                }
            }
        }
        return true;
    }

    private void generateFromInterface(TypeElement typeElement, String packageName, Builder annotation,
            String builderClassName, PrintWriter out) {
        List<Parameter> parameters = typeElement //
                .getEnclosedElements() //
                .stream() //
                .filter(x -> x.getKind() == ElementKind.METHOD) //
                .map(x -> (ExecutableElement) x)
                .map(x -> new Parameter(x.getReturnType().toString(), x.getSimpleName().toString()))
                .collect(Collectors.toList());

    }

    private void generateFromClassOrRecord(TypeElement typeElement, String packageName, Builder annotation,
            String builderClassName, PrintWriter out) {
        String builderPackageName = Util.pkg(builderClassName);
        ExecutableElement constructor = constructor(typeElement);
        List<Parameter> parameters = constructor //
                .getParameters() //
                .stream() //
                .map(p -> new Parameter(p.asType().toString(), p.getSimpleName().toString()))
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
                annotation.alwaysIncludeBuildMethod()));
        out.println();
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