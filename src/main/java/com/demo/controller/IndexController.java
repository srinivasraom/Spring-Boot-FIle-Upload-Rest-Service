package com.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Controller for root paths to route requests */
@Controller
public class IndexController {

	@GetMapping("/")
	public String index() {
		return "upload";
	}

}