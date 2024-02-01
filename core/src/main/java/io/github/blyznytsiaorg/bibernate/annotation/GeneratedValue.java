package io.github.blyznytsiaorg.bibernate.annotation;

import static io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType.*;

import io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl.GenerationType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GeneratedValue {
  GenerationType strategy() default IDENTITY;
  String generator() default "";
}
