package ru.demo.documentsapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class MainController {
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/details")
    public String details(@RequestParam("id") UUID id) {
        return "document-details";
    }

    @GetMapping("/document-add-update")
    public String detailsAddUpdate(@RequestParam(value = "id", required = false) UUID id) {
        return "document-add-update";
    }
}
