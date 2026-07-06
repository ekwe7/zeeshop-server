package com.ekwe_hub.zeeshopserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "systemAuditorAware")
public class ZeeshopServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZeeshopServerApplication.class, args);
    }

}
