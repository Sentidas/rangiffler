package ru.sentidas.rangiffler.jupiter.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface User {

    // По умолчанию false —  минимальный user (username + countryCode).
    // true - username, countryCode, avatar, firstName, surname
    boolean full() default false;

    String username() default "";

    // Явные фото для конкретных владельцев (SELF/FRIEND/INCOME/OUTCOME)
    Photo[] photos() default {};

    // Счётчик фото для самого пользователя (SELF)
    int photo() default 0;

    // Друзья
    int friends() default 0;  // минимальный
    int fullFriends() default 0; // полный

    // Входящие инвайты (меня пригласили в друзья)
    int incomeInvitation() default 0; // минимальный
    int fullIncomeInvitation() default 0; // полный

    // Исходящие инвайты (я приглашаю в друзья)
    int outcomeInvitation() default 0; // минимальный
    int fullOutcomeInvitation() default 0; // полный


    // добавление рандомных фото, указывается количество фото каждому из
    // аннотаций friends()/incomeInvitation()/outcomeInvitation() или полных
    // используется только вместе с соответствующими аннотациями()
    int friendsWithPhotosEach() default 0;
    int incomeWithPhotosEach() default 0;
    int outcomeWithPhotosEach() default 0;

}
