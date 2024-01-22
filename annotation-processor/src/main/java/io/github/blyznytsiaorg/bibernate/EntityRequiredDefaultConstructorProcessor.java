package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.util.Set;

@SupportedAnnotationTypes("io.github.blyznytsiaorg.bibernate.annotation.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class EntityRequiredDefaultConstructorProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;

                if (!hasNoArgsConstructor(typeElement)) {
                    messager.printMessage(javax.tools.Diagnostic.Kind.ERROR,
                            "Class annotated with @Entity must have constructor without params", typeElement);
                }
            }
        }

        return true;
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
