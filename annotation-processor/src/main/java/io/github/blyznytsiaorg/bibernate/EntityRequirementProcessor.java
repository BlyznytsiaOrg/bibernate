package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Objects;
import java.util.Set;

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
        roundEnv.getElementsAnnotatedWith(Entity.class)
                .stream()
                .filter(element -> element.getKind() == ElementKind.CLASS)
                .forEach(element -> {
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
                });

        return true;
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
