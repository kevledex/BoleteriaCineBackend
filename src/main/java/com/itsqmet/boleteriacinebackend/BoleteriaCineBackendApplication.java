package com.itsqmet.boleteriacinebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BoleteriaCineBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoleteriaCineBackendApplication.class, args);
    }

}
