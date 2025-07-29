package com.digi.auth.controller;

import com.digi.auth.config.AuthConfig;
import com.digi.auth.form.UserForm;
import com.digi.auth.model.User;
import com.digi.auth.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class FrontController {

    @Autowired
    private UserService userservice;
    @Autowired
    private AuthConfig authConfig;

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        String authType = authConfig.getType();
        model.addAttribute("user", new UserForm());
        model.addAttribute("auth",authType);
        return "login";
    }

    @PostMapping("/login")
    public String authenticateUser(
            @ModelAttribute("user") @Valid UserForm userform,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {

        if (bindingResult.hasErrors()) {
            System.out.println("Validation Errors: " + bindingResult.getAllErrors());
            return "login";
        }

        User user = userservice.findByEmailAndPassword(userform.getEmail(), userform.getPassword());

        if (user != null) {
            session.setAttribute("user", user);
            model.addAttribute("user", user);
            user.setStatus("online");
            userservice.saveUser(user);
            return "redirect:dashboard";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");
        if (loggedInUser != null) {
            model.addAttribute("user", loggedInUser);
            List<User> users = userservice.fetchAllUsers();
            model.addAttribute("users", users);
            return "dashboard";
        } else {
            return "redirect:/logout";
        }
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        User user = (User) session.getAttribute("user");
        user.setStatus("offline");
        userservice.saveUser(user);
        session.invalidate();

        return "redirect:/login";
    }
}
