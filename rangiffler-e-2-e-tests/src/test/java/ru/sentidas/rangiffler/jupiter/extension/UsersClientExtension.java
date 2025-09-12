package ru.sentidas.rangiffler.jupiter.extension;


import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import ru.sentidas.rangiffler.service.UsersClient;
import ru.sentidas.rangiffler.service.UsersDbClient;

import java.lang.reflect.Field;

public class UsersClientExtension implements TestInstancePostProcessor {
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.getType().isAssignableFrom(UsersClient.class)) {
                field.setAccessible(true);
                field.set(testInstance, new UsersDbClient());
               // field.set(testInstance, "api".equals(System.getProperty("client.impl"))
                      //  ? new UsersApiClient()
                     //   : new UsersDbClient());
            }
        }
    }
}
