package vn.iotstar.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class AuthController {
 @GetMapping("/") public String home(){return "redirect:/login";}
 @GetMapping("/login") public String login(){return "login";}
 @GetMapping("/user/profile") public String profile(){return "profile";}
}

