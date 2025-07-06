package com.saga.infrastructure.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for serving dashboard views.
 */
@Slf4j
@Controller
public class DashboardViewController {

    /**
     * Serve the main dashboard page
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        log.info("Serving dashboard page");
        return "dashboard";
    }

    /**
     * Redirect root to dashboard
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }
} 