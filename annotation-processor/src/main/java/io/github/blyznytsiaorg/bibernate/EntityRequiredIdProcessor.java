package io.github.blyznytsiaorg.bibernate;

import io.github.blyznytsiaorg.bibernate.annotation.Entity;
import io.github.blyznytsiaorg.bibernate.annotation.Id;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("io.github.blyznytsiaorg.bibernate.annotation.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class EntityRequiredIdProcessor extends AbstractProcessor {

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
                boolean hasRequiredField = element.getEnclosedElements().stream()
                        .anyMatch(field -> field.getKind() == ElementKind.FIELD && field.getAnnotation(Id.class) != null);

                if (!hasRequiredField) {
                    messager.printMessage(javax.tools.Diagnostic.Kind.ERROR,
                            "Class annotated with @Entity must have at least one field annotated with @Id",
                            element);
                }
            }
        }

        return true;
    }
}
