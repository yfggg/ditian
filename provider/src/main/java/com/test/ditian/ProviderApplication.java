package com.test.ditian;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
//@EntityScan(value = {"com.test.ditian.*"})
//@ComponentScan(basePackages = "com.test.ditian.*")
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
        log.info("http://127.0.0.1:8001/swagger-ui.html");
    }

}
