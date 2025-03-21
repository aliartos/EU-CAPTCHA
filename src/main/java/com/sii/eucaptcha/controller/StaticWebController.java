package com.sii.eucaptcha.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving the static web pages
 */
@Controller
public class StaticWebController {

    /**
     * Handles the root path and serves the index.html page
     * @return the name of the view to render
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * Handles the /demo path and serves the index.html page
     * @return the name of the view to render
     */
    @GetMapping("/demo")
    public String demo() {
        return "forward:/index.html";
    }
}