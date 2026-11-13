package com.lcms.controller;

import com.lcms.model.User;
import com.lcms.model.enums.Department;
import com.lcms.model.enums.Role;
import com.lcms.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("roles", Role.values());
        model.addAttribute("departments", Department.values());
        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(
            @RequestParam("username") String username,
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            @RequestParam("role") Role role,
            @RequestParam(value = "departments", required = false) List<Department> departments,
            RedirectAttributes redirectAttributes) {

        try {
            Set<Department> deptSet = departments != null ? new HashSet<>(departments) : new HashSet<>();
            User newUser = new User(username, password, fullName, phone, role, deptSet);
            userService.createUser(newUser);
            redirectAttributes.addFlashAttribute("successMessage", "User account created successfully for " + username);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-password")
    public String resetPassword(
            @PathVariable("id") Long id,
            @RequestParam("newPassword") String newPassword,
            RedirectAttributes redirectAttributes) {

        try {
            userService.adminResetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        userService.findAllUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .ifPresent(u -> {
                    u.setActive(!u.isActive());
                    userService.updateUser(u);
                    redirectAttributes.addFlashAttribute("successMessage", 
                            "Status for " + u.getUsername() + " changed to " + (u.isActive() ? "Active" : "Suspended"));
                });

        return "redirect:/admin/users";
    }
}
