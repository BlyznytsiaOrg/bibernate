package io.github.blyznytsiaorg.bibernate.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(value={TYPE,METHOD,FIELD})
@Retention(value=RUNTIME)
public @interface SequenceGenerator {

  String name();
  String sequenceName();
  int initialValue() default 1;
  int allocationSize() default 1;


}
