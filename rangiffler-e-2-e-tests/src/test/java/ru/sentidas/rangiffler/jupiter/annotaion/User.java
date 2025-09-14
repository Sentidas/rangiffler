package ru.sentidas.rangiffler.jupiter.annotaion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.sentidas.rangiffler.jupiter.extension.PhotoExtension;
import ru.sentidas.rangiffler.jupiter.extension.UserExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface User {

    String username() default "";

    Photo[] photos() default {};

    int photo() default 0;

    int friends() default 0;
    int friendsWithPhotos() default 0;

    int incomeInvitation() default 0;
    int incomeInvitationWithPhotos() default 0;

    int outcomeInvitation() default 0;
    int outcomeInvitationWithPhotos() default 0;

}
