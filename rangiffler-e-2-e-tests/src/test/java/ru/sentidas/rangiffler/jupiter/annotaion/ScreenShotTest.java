package ru.sentidas.rangiffler.jupiter.annotaion;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
public @interface ScreenShotTest {

    String value() default "";

    String[] files() default  {};

    boolean rewriteExpected() default false;
}
