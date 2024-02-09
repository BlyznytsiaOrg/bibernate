package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.IgnoreEntity;
import io.github.blyznytsiaorg.bibernate.annotation.SequenceGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Annotation processor for validating requirements on classes annotated with the {@link io.github.blyznytsiaorg.bibernate.annotation.Entity} annotation.
 * This processor performs checks such as ensuring the presence of a field annotated with {@link io.github.blyznytsiaorg.bibernate.annotation.Id},
 * the existence of a no-args constructor, and the consistency of generator names between {@link io.github.blyznytsiaorg.bibernate.annotation.GeneratedValue}
 * and {@link io.github.blyznytsiaorg.bibernate.annotation.SequenceGenerator} annotations.
 * <p>
 * Additionally, this processor supports the {@link io.github.blyznytsiaorg.bibernate.annotation.IgnoreEntity} annotation to exclude specific classes
 * from the validation process.
 *
 * @author Blyzhnytsia Team
 * @since 1.0
 */
@SupportedAnnotationTypes("io.github.blyznytsiaorg.bibernate.annotation.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class EntityRequirementProcessor extends AbstractProcessor {

    /**
     * The Messager used for reporting diagnostic messages during the annotation processing.
     * It provides a way for the annotation processor to communicate messages, warnings, and errors back to the user
     * or the development environment.
     */
    private Messager messager;

    /**
     * Initializes the annotation processor by obtaining the Messager from the processing environment.
     *
     * @param processingEnv The processing environment providing access to various utility methods and services.
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    /**
     * Processes the annotated elements and performs validation checks on classes annotated with @Entity.
     * Supports the @IgnoreEntity annotation to exclude specific classes from validation.
     *
     * @param annotations The set of annotation types that this processor supports.
     * @param roundEnv    The environment for a round of annotation processing.
     * @return True if the set of annotations are claimed by this processor, false otherwise.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> ignoreEntities = roundEnv.getElementsAnnotatedWith(IgnoreEntity.class);

        // Get class names of entities to be ignored
        Set<String> ignoredEntityNames = ignoreEntities.stream()
                .filter(element -> element.getKind() == ElementKind.CLASS)
                .map(Element::getSimpleName)
                .map(Name::toString)
                .collect(Collectors.toSet());

        roundEnv.getElementsAnnotatedWith(Entity.class)
                .stream()
                .filter(element -> element.getKind() == ElementKind.CLASS)
                .filter(element -> !ignoredEntityNames.contains(element.getSimpleName().toString()))
                .forEach(this::validate);

        return true;
    }

    /**
     * Validates the requirements on a class annotated with @Entity.
     *
     * @param element The annotated element to be validated.
     */
    private void validate(Element element) {
        TypeElement typeElement = (TypeElement) element;

        boolean hasRequiredField = element.getEnclosedElements()
                .stream()
                .anyMatch(this::isIdAnnotatedField);

        boolean hasMismatchInGeneratorName = element.getEnclosedElements()
                .stream()
                .anyMatch(this::isMismatchInGeneratorName);

        if (!hasRequiredField) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Class annotated with @Entity must have at least one field annotated with @Id",
                    element);
        }

        if (!hasNoArgsConstructor(typeElement)) {
            messager.printMessage(javax.tools.Diagnostic.Kind.ERROR,
                    "Class annotated with @Entity must have constructor without params", typeElement);
        }

        if (hasMismatchInGeneratorName) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    ("In class `%s` in @GeneratedValue annotation generator name do not match name "
                     + "in @SequenceGenerator annotation").formatted(typeElement));
        }
    }

    /**
     * Checks if there is a mismatch in generator names between @GeneratedValue and @SequenceGenerator annotations.
     *
     * @param field The field element to be checked for the mismatch.
     * @return True if a mismatch in generator names is found, false otherwise.
     */
    private boolean isMismatchInGeneratorName(Element field) {
        GeneratedValue generatedValueAnnotation = field.getAnnotation(GeneratedValue.class);
        if (generatedValueAnnotation != null) {
            String generatorName = generatedValueAnnotation.generator();
            if (generatorName != null && !generatorName.isEmpty()) {
                SequenceGenerator sequenceGeneratorAnnotation = field.getAnnotation(SequenceGenerator.class);
                if (sequenceGeneratorAnnotation != null) {
                    String generNameInSequenceGeneratorAnnotation = sequenceGeneratorAnnotation.name();
                    return field.getKind() == ElementKind.FIELD
                           && !generatorName.equals(generNameInSequenceGeneratorAnnotation);
                }
            }
        }
        return false;
    }

    /**
     * Checks if a field is annotated with {@link io.github.blyznytsiaorg.bibernate.annotation.Id}.
     *
     * @param field The field element to be checked.
     * @return True if the field is annotated with @Id, false otherwise.
     */
    private boolean isIdAnnotatedField(Element field) {
        return field.getKind() == ElementKind.FIELD && Objects.nonNull(field.getAnnotation(Id.class));
    }

    /**
     * Checks if a class has a no-args constructor (constructor without parameters).
     *
     * @param typeElement The TypeElement representing the class to be checked.
     * @return True if the class has a no-args constructor, false otherwise.
     */
    private boolean hasNoArgsConstructor(TypeElement typeElement) {
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR &&
                (enclosedElement.getModifiers().contains(Modifier.PUBLIC) &&
                 ((ExecutableElement) enclosedElement).getParameters().isEmpty())) {
                return true;
            }
        }

        return false;
    }
}
