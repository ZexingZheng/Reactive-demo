package com.example.reactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@Configuration
public class EmailConfig {

    /**
     * Create dedicated thread pool scheduler for email sending
     * Because JavaMailSender is blocking, it needs to be executed in a separate thread pool
     */
    @Bean(name = "emailScheduler")
    public Scheduler emailScheduler() {
        return Schedulers.fromExecutor(
            Executors.newFixedThreadPool(10, r -> {
                Thread thread = new Thread(r);
                thread.setName("email-sender-");
                thread.setDaemon(true);
                return thread;
            })
        );
    }
}
