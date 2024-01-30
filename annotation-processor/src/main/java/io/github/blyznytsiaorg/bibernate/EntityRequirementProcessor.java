package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.IgnoreEntity;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("io.github.blyznytsiaorg.bibernate.annotation.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class EntityRequirementProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

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
    
    private void validate(Element element) {
        TypeElement typeElement = (TypeElement) element;
        
        boolean hasRequiredField = element.getEnclosedElements()
                .stream()
                .anyMatch(this::isIdAnnotatedField);

        if (!hasRequiredField) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Class annotated with @Entity must have at least one field annotated with @Id",
                    element);
        }

        if (!hasNoArgsConstructor(typeElement)) {
            messager.printMessage(javax.tools.Diagnostic.Kind.ERROR,
                    "Class annotated with @Entity must have constructor without params", typeElement);
        }
    }
    
    private boolean isIdAnnotatedField(Element field) {
        return field.getKind() == ElementKind.FIELD && Objects.nonNull(field.getAnnotation(Id.class));
    }

    private boolean hasNoArgsConstructor(TypeElement typeElement) {
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                if (enclosedElement.getModifiers().contains(Modifier.PUBLIC) &&
                    ((ExecutableElement) enclosedElement).getParameters().isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }
}
