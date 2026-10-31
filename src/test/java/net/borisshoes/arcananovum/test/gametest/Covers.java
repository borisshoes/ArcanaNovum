package net.borisshoes.arcananovum.test.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The achievement or augment ids a game test covers. The coverage tests fail when an id has no test and no written reason */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Covers {
   String[] value();
}
