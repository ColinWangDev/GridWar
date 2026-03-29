package com.gridwar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GridWarApplication {

    public static void main(String[] args) {
        SpringApplication.run(GridWarApplication.class, args);
    }
}
