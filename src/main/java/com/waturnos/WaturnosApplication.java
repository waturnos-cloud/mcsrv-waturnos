package com.waturnos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WaturnosApplication {
    public static void main(String[] args) {
        SpringApplication.run(WaturnosApplication.class, args);
    }
}
