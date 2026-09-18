package com.example.archive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController { @GetMapping({"/", "/projects", "/about", "/contact"}) public String index() { return "index"; } }
