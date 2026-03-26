package at.spengergasse.spring_thymeleaf.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class HelloController {

    @GetMapping("")
    public String index(Model model) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        model.addAttribute("today", LocalDate.now().format(dtf));
        return "index";
    }
}