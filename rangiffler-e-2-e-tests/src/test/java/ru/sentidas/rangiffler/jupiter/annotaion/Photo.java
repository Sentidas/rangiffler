package ru.sentidas.rangiffler.jupiter.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Photo {

    String countryCode() default "";  // страна поездки
    String description() default "";  // описание (если пусто — сгенерим по локали владельца)
    String src() default "";          // src (если пусто — classpath через Gen.randomPhotoDataUrl)

    // лайки: user <= friends; friends == 1 (от основного пользователя); outcome/income - запрет
    int likes() default 0;
    int count() default 1;            // сколько фото создать
    Owner owner() default Owner.USER; // владелец фото
    int partyIndex() default -1;      // индекс друга/инвайта при owner != USER

    enum Owner {USER, FRIEND, INCOME, OUTCOME}
}
