package com.example.reactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@Configuration
public class EmailConfig {

    /**
     * 为邮件发送创建专用的线程池调度器
     * 因为 JavaMailSender 是阻塞的，需要在独立的线程池中执行
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
