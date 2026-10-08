package com.studymind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point for StudyMind AI Application.
 *
 * OOP / Viva Concept:
 * @SpringBootApplication encapsulates 3 key annotations:
 * 1. @Configuration (marks class as bean definitions source)
 * 2. @EnableAutoConfiguration (Spring automatically wires up embedded server, thymeleaf, etc.)
 * 3. @ComponentScan (automatically scans sub-packages like controller, service, repository)
 */
@SpringBootApplication
public class StudyMindApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyMindApplication.class, args);
        System.out.println("=================================================");
        System.out.println(" StudyMind AI Server Started Successfully!      ");
        System.out.println(" URL: http://localhost:8080                      ");
        System.out.println("=================================================");
    }
}
