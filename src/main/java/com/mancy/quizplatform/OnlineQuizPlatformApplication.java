package com.mancy.quizplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Entry point. Run this class (or use ./mvnw spring-boot:run) to start the website. */
@SpringBootApplication
@EnableScheduling   // needed for the background job that closes expired quiz attempts
public class OnlineQuizPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineQuizPlatformApplication.class, args);
    }
}
