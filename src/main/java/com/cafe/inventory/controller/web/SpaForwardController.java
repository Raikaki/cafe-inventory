package com.cafe.inventory.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards client-side (React Router) routes to the SPA entry point so that
 * deep links / page refreshes return index.html instead of 404.
 * REST endpoints (/api/**) and static assets (paths containing a dot) are not matched.
 */
@Controller
public class SpaForwardController {

    @GetMapping(value = {"/", "/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
