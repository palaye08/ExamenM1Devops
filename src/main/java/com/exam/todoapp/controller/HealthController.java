package com.exam.todoapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Contrôleur de santé simple pour vérifier que l'application est démarrée.
 * L'endpoint /actuator/health est aussi disponible via Spring Boot Actuator.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of(
            "message", "Application TP DevOps Exam - déployée avec succès !",
            "status", "UP"
        );
    }
}
