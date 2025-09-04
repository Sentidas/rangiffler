package ru.sentidas.rangiffler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.sentidas.rangiffler.service.PropertiesLogger;

@SpringBootApplication
public class RangifflerGeoApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(RangifflerGeoApplication.class);
        springApplication.addListeners(new PropertiesLogger());
        springApplication.run(args);
    }
}
