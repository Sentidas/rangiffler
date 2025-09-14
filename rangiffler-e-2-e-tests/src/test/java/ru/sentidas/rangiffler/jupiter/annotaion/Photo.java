package ru.sentidas.rangiffler.jupiter.annotaion;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.sentidas.rangiffler.jupiter.extension.PhotoExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Photo {

    String countryCode() default "";

    String description() default "";

    String src() default "";

    int likes() default 0;
}
