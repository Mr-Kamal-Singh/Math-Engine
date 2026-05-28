
package com.engine.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.engine")
public class Main {
    public static void main(String[] args) {
        // This single line ignites the Spring framework, starts the embedded Tomcat
        // server, and prepares our app to receive HTTP traffic.
        SpringApplication.run(Main.class, args);
        System.out.println("\n=======================================================");
        System.out.println("  MATH ENGINE MICROSERVICE INITIALIZED ON PORT 8080");
        System.out.println("=======================================================\n");
    }
}