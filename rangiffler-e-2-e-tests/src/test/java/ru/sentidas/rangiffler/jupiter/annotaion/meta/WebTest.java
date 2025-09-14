package ru.sentidas.rangiffler.jupiter.annotaion.meta;

import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.sentidas.rangiffler.jupiter.extension.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith({
        BrowserExtension.class,
        AllureJunit5.class,
        UserExtension.class,
        ApiLoginExtension.class,
        PhotoExtension.class
//   ScreenShotTestExtension.class
})
public @interface WebTest {
}
