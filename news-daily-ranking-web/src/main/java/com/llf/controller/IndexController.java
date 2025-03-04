package com.llf.controller;

import com.llf.aspect.visit.VisitLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    @VisitLog
    public String index(){
        return "index";
    }
}
