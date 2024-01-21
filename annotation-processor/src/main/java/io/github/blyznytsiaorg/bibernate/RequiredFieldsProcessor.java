package io.github.blyznytsiaorg.bibernate;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("EntityRequired")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class RequiredFieldsProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(EntityRequired.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                // Check if the class has at least one field annotated with @Required
                boolean hasRequiredField = element.getEnclosedElements().stream()
                        .anyMatch(field -> field.getKind() == ElementKind.FIELD
                                && field.getAnnotation(IdField.class) != null);

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
