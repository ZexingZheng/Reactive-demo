package com.reactive.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ReactiveDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReactiveDemoApplication.class, args);
    }
}
