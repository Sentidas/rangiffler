package ru.sentidas.rangiffler.jupiter.annotaion;

import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.model.Photo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Test
public @interface User {

    String username() default "";

   // Photo[] photos() default {};

    int friends() default 0;

    int incomeInvitation() default 0;

    int outcomeInvitation() default 0;

}
