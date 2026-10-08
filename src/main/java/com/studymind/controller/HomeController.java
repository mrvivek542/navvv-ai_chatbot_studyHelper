package com.studymind.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController serves the initial landing/dashboard page.
 *
 * OOP / Viva Concept:
 * - Separation of Concerns: The controller only handles routing HTTP requests.
 * - Model passing: We pass dynamic data to Thymeleaf via Spring's Model interface.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", "StudyMind AI");
        model.addAttribute("tagline", "Java-Powered Context-Aware Intelligent Study Assistant");
        model.addAttribute("status", "System Online & Ready");
        return "index";
    }
}
