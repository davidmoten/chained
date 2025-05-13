package com.github.davidmoten.chained.processor;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.github.davidmoten.chained.api.Builder;
import com.github.davidmoten.chained.api.BuilderConstructor;
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

        createClass(wrappedTypeElement, annotation);

    }

    @DeclareCompilerMessage(code = "ERROR_002", enumValueName = "ERROR_VALUE_MUST_NOT_BE_EMPTY", message = "Value must not be empty")
    @DeclareCompilerMessage(code = "ERROR_003", enumValueName = "ERROR_TYPE_MUST_BE_CLASS_OR_RECORD", message = "Type must be class or record")
    boolean validateUsage(TypeElementWrapper wrappedTypeElement, BuilderWrapper annotation) {

//        boolean result = wrappedTypeElement.validateWithFluentElementValidator() //
//                .is(AptkCoreMatchers.IS_CLASS)
//                // .applyValidator(AptkCoreMatchers.HAS_PUBLIC_NOARG_CONSTRUCTOR)
//                .validateAndIssueMessages() ||
//                wrappedTypeElement.validateWithFluentElementValidator() //
//                .is(AptkCoreMatchers.IS_RECORD)
//                // .applyValidator(AptkCoreMatchers.HAS_PUBLIC_NOARG_CONSTRUCTOR)
//                .validateAndIssueMessages()
//                ;

        boolean result = wrappedTypeElement.isClass() //
                || wrappedTypeElement.isRecord();
        System.out.println(">>>>>>> " + wrappedTypeElement.getQualifiedName() + " " + result);
        if (!result) {
            wrappedTypeElement.compilerMessage().asError()
                    .write(BuilderProcessorCompilerMessages.ERROR_TYPE_MUST_BE_CLASS_OR_RECORD);
        } else if (annotation.value().isEmpty()) {
            wrappedTypeElement.compilerMessage().asError()
                    .write(BuilderProcessorCompilerMessages.ERROR_VALUE_MUST_NOT_BE_EMPTY);
            result = false;
        }
        return result;

    }

    /**
     * Generates a class.
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

        String builderPackageName = Util.pkg(builderClassName);

        // create the class
        try {
            SimpleJavaWriter w = FilerUtils.createSourceFile(builderClassName, wrappedTypeElement.unwrap());
            ExecutableElementWrapper constructor = constructor(wrappedTypeElement);
            List<Parameter> list = constructor //
                    .getParameters() //
                    .stream() //
                    .map(f -> {
                        String type = f.asType().toString();
                        String name = f.getSimpleName().toString();
                        return new Parameter(type, name);
                    }).collect(Collectors.toList());
            boolean constructorVisible = //
                    wrappedTypeElement.getModifiers().contains(Modifier.PUBLIC) //
                            || //
                            wrappedTypeElement.getModifiers().contains(Modifier.DEFAULT)
                                    && wrappedTypeElement.getPackageName().equals(builderPackageName);
            w.append(Generator.chainedBuilder( //
                    wrappedTypeElement.getQualifiedName(), //
                    builderClassName, //
                    list, //
                    constructorVisible));
            w.close();
        } catch (IOException e) {
            wrappedTypeElement.compilerMessage().asError().write(
                    BuilderProcessorCompilerMessages.ERROR_COULD_NOT_CREATE_CLASS, builderClassName, e.getMessage());
        }
    }

    private static ExecutableElementWrapper constructor(TypeElementWrapper element) {
        List<ExecutableElementWrapper> list = element.getConstructors() //
                .stream() //
                .filter(c -> c.getModifiers().contains(Modifier.PUBLIC)) //
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
                        "Multiple max-length public constructors found, there should be just one with maximum length or one with BuilderConstructor annotation");
            }
            return max;
        }
    }

}
