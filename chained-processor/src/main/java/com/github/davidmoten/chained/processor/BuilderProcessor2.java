package com.github.davidmoten.chained.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
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

import com.github.davidmoten.chained.api.Builder;
import com.github.davidmoten.chained.api.BuilderConstructor;
import com.github.davidmoten.chained.processor.Generator.Parameter;

@SupportedAnnotationTypes("com.github.davidmoten.chained.api.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BuilderProcessor2 extends AbstractProcessor {

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
                String templatedBuilderClassName = typeElement.getAnnotation(Builder.class).value();
                String builderClassName = templatedBuilderClassName //
                        .replace("${pkg}", packageName) //
                        .replace("${simpleName}", simpleClassName);
                String builderPackageName = Util.pkg(builderClassName);

                try {
                    Filer filer = processingEnv.getFiler();
                    JavaFileObject file = filer.createSourceFile(builderClassName);
                    try (PrintWriter out = new PrintWriter(file.openWriter())) {
                        ExecutableElement constructor = constructor(typeElement);
                        List<Parameter> parameters = constructor //
                                .getParameters() //
                                .stream() //
                                .map(p -> new Parameter(p.asType().toString(), p.getSimpleName().toString()))
                                .collect(Collectors.toList());

                        boolean constructorVisible = //
                                typeElement.getModifiers().contains(Modifier.PUBLIC) //
                                        || //
                                        typeElement.getModifiers().contains(Modifier.DEFAULT)
                                                && packageName.equals(builderPackageName);
                        out.print(Generator.chainedBuilder( //
                                typeElement.getQualifiedName().toString(), //
                                builderClassName, //
                                parameters, //
                                constructorVisible));
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException("Could not create class " + builderClassName + ": " + e.getMessage(),
                            e);
                }
            }
        }
        return true;
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