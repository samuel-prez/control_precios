package com.ritchi.control_precios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ControlPreciosApplication {
    public static void main(String[] args) {
        SpringApplication.run(ControlPreciosApplication.class, args);
    }
}