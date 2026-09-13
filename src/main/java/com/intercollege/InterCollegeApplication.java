package com.intercollege;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the InterCollege Spring Boot application.
 *
 * NOTE FOR FIRST-YEAR COMPUTER SCIENCE STUDENTS:
 * The @SpringBootApplication annotation enables:
 * 1. @Configuration: Tags the class as a source of bean definitions.
 * 2. @EnableAutoConfiguration: Tells Spring Boot to configure beans based on classpath settings.
 * 3. @ComponentScan: Scans for other components, configurations, and services in this package.
 */
@SpringBootApplication
public class InterCollegeApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterCollegeApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("   InterCollege Web Application is running!");
        System.out.println("   Access in browser: http://localhost:8080");
        System.out.println("==========================================================");
    }
}
