package com.github.davidmoten.chained.processor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.github.davidmoten.chained.api.Builder;
import com.github.davidmoten.chained.api.BuilderConstructor;
import com.github.davidmoten.chained.api.BuilderIgnore;
import com.github.davidmoten.chained.processor.Generator.Parameter;

import io.toolisticon.aptk.compilermessage.api.DeclareCompilerMessage;
import io.toolisticon.aptk.compilermessage.api.DeclareCompilerMessageCodePrefix;
import io.toolisticon.aptk.tools.AbstractAnnotationProcessor;
import io.toolisticon.aptk.tools.FilerUtils;
import io.toolisticon.aptk.tools.corematcher.AptkCoreMatchers;
import io.toolisticon.aptk.tools.generators.SimpleJavaWriter;
import io.toolisticon.aptk.tools.wrapper.ExecutableElementWrapper;
import io.toolisticon.aptk.tools.wrapper.TypeElementWrapper;
import io.toolisticon.spiap.api.SpiService;

/**
 * Annotation Processor for {@link com.github.davidmoten.chained.api.Builder}.
 *
 * This demo processor does some validations and creates a class.
 */

@SpiService(Processor.class)
@DeclareCompilerMessageCodePrefix("Builder")
public class BuilderProcessor extends AbstractAnnotationProcessor {

    private final static Set<String> SUPPORTED_ANNOTATIONS = createSupportedAnnotationSet(Builder.class);

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return SUPPORTED_ANNOTATIONS;
    }

    @Override
    public boolean processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (!roundEnv.processingOver()) {
            // process Services annotation
            for (Element element : roundEnv.getElementsAnnotatedWith(Builder.class)) {

                TypeElementWrapper wrappedTypeElement = TypeElementWrapper.wrap((TypeElement) element);
                BuilderWrapper annotation = BuilderWrapper.wrap(wrappedTypeElement.unwrap());

                if (validateUsage(wrappedTypeElement, annotation)) {
                    processAnnotation(wrappedTypeElement, annotation);
                }

            }

        } else {

            // ProcessingOver round

        }
        return false;

    }

    void processAnnotation(TypeElementWrapper wrappedTypeElement, BuilderWrapper annotation) {

        // ----------------------------------------------------------
        // TODO: replace the following code by your business logic
        // ----------------------------------------------------------

        createClass(wrappedTypeElement, annotation);

    }

    @DeclareCompilerMessage(code = "ERROR_002", enumValueName = "ERROR_VALUE_MUST_NOT_BE_EMPTY", message = "Value must not be empty")
    boolean validateUsage(TypeElementWrapper wrappedTypeElement, BuilderWrapper annotation) {

        // ----------------------------------------------------------
        // TODO: replace the following code by your business logic
        // ----------------------------------------------------------

        // Some example validations : Annotation may only be applied on Classes with
        // Noarg constructor.
        boolean result = wrappedTypeElement.validateWithFluentElementValidator().is(AptkCoreMatchers.IS_CLASS)
                // .applyValidator(AptkCoreMatchers.HAS_PUBLIC_NOARG_CONSTRUCTOR)
                .validateAndIssueMessages();

        if (annotation.value().isEmpty()) {
            wrappedTypeElement.compilerMessage().asError()
                    .write(BuilderProcessorCompilerMessages.ERROR_VALUE_MUST_NOT_BE_EMPTY);
            result = false;
        }
        return result;

    }

    /**
     * Generates a class.
     *
     * Example how to use the templating engine.
     *
     * TODO: remove this
     *
     * @param wrappedTypeElement The TypeElement representing the annotated class
     * @param annotation         The Builder annotation
     */
    @DeclareCompilerMessage(code = "ERROR_001", enumValueName = "ERROR_COULD_NOT_CREATE_CLASS", message = "Could not create class ${0} : ${1}")
    private void createClass(TypeElementWrapper wrappedTypeElement, BuilderWrapper annotation) {

        String templatedFullClassName = annotation.value();
        String builderClassName = templatedFullClassName //
                .replace("${pkg}", wrappedTypeElement.getPackageName()) //
                .replace("${simpleName}", wrappedTypeElement.getSimpleName());

        // Extract components for templating the class
        String packageName = Util.pkg(builderClassName);
        String simpleClassName = Util.simpleClassName(builderClassName);

        // Fill Model
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("packageName", packageName);
        model.put("className", simpleClassName);

        // create the class
        try {
            SimpleJavaWriter javaWriter = FilerUtils.createSourceFile(builderClassName, wrappedTypeElement.unwrap());
            List<Parameter> list = constructor(wrappedTypeElement) //
                    .getParameters() //
                    .stream() //
                    .map(f -> {
                        String type = f.asType().toString();
                        String name = f.getSimpleName().toString();
                        return new Parameter(type, name);
                    }).collect(Collectors.toList());
            javaWriter.append(Generator.chainedBuilder(wrappedTypeElement.getQualifiedName(), builderClassName, list));
            javaWriter.close();
        } catch (IOException e) {
            wrappedTypeElement.compilerMessage().asError().write(
                    BuilderProcessorCompilerMessages.ERROR_COULD_NOT_CREATE_CLASS, builderClassName, e.getMessage());
        }
    }

    private static ExecutableElementWrapper constructor(TypeElementWrapper element) {
        List<ExecutableElementWrapper> list = element.getConstructors() //
                .stream() //
                .filter(c -> c.getModifiers().contains(Modifier.PUBLIC)) //
                .filter(c -> !c.getAnnotation(BuilderIgnore.class).isPresent()) //
                .collect(Collectors.toList());

        List<ExecutableElementWrapper> defined = list //
                .stream() //
                .filter(c -> c.getAnnotation(BuilderConstructor.class).isPresent()) //
                .collect(Collectors.toList());
        if (defined.size() > 1) {
            throw new IllegalStateException("Multiple constructors with BuilderConstructor annotation found");
        } else if (defined.size() == 1) {
            return defined.get(0);
        } else {
            ExecutableElementWrapper max = list.stream()
                    .max((c1, c2) -> Integer.compare(c1.getParameters().size(), c2.getParameters().size()))
                    .orElseThrow(() -> new IllegalStateException("No public constructor found"));
            if (list.stream().filter(c -> c.getParameters().size() == max.getParameters().size()).count() > 1) {
                throw new IllegalStateException(
                        "Multiple max-length public constructors found, there should be just one with maximum length without a BuilderIgnore annotation");
            }
            return max;
        }
    }

}
