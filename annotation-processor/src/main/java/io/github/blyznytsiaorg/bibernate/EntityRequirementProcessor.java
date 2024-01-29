package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;
import io.github.blyznytsiaorg.bibernate.annotation.OneToOne;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.*;

@SupportedAnnotationTypes("io.github.blyznytsiaorg.bibernate.annotation.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class EntityRequirementProcessor extends AbstractProcessor {

    private Messager messager;
    private Set<TypeMirror> entities = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Entity.class)
                .forEach(element -> entities.add(element.asType()));

        roundEnv.getElementsAnnotatedWith(Entity.class)
                .stream()
                .filter(element -> element.getKind() == ElementKind.CLASS)
                .forEach(this::validate);

        return true;
    }

    private void validate(Element element) {
        TypeElement typeElement = (TypeElement) element;

//        list.add(typeElement.getSimpleName().toString());
        boolean hasRequiredField = element.getEnclosedElements()
                .stream()
                .anyMatch(this::isIdAnnotatedField);

        if (!hasRequiredField) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Class annotated with @Entity must have at least one field annotated with @Id",
                    element);
        }

        if (!hasNoArgsConstructor(typeElement)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Class annotated with @Entity must have constructor without params", typeElement);
        }

        if (!hasRelationAnnotationOnEntityField(typeElement)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    entities.toString() + "Entity field should have relation annotation", typeElement);
        }
    }


    private boolean hasRelationAnnotationOnEntityField(TypeElement typeElement) {
        for (Element enclosedElement : typeElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                VariableElement variableElement = (VariableElement) enclosedElement;

//                messager.printMessage(Diagnostic.Kind.ERROR,
//                        list.toString());
//                messager.printMessage(Diagnostic.Kind.ERROR,
//                        list.toString() + "Just showing list ot classes", typeElement);


//                if (entities.contains(variableElement.asType())) {
//                    if (!Objects.nonNull(variableElement.getAnnotation(OneToOne.class))) {
//                        return false;
//                    }
//                }

            }
        }

        return true;
    }

//    private boolean hasOneToOneAnnotation(VariableElement variableElement) {
//        for (AnnotationMirror annotationMirror : variableElement.getAnnotationMirrors()) {
//            TypeElement annotationElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
//
//            if (annotationElement.getClass().isAnnotationPresent(Entity.class)) {
//                // Check if the annotation is @OneToOne
//                return Objects.nonNull(variableElement.getAnnotation(OneToOne.class));
//                }
//            }
//
//        return true;
//    }

    //    private boolean hasOneToOneAnnotation(TypeElement typeElement) {
//        return elementUtils.getTypeElement(OneToOne.class.getCanonicalName())
//                .equals(elementUtils.getTypeElement(typeElement.getQualifiedName()));
//    }
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
