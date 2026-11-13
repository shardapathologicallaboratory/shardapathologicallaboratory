package com.lcms.controller;

import com.lcms.model.User;
import com.lcms.model.enums.Department;
import com.lcms.security.CustomUserDetails;
import com.lcms.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElse(userDetails.getUser());
        model.addAttribute("currentUser", user);
        model.addAttribute("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        
        // Operational daily summary counters (seeded stubs for dashboard view)
        model.addAttribute("totalPatientsToday", 48);
        model.addAttribute("walkInCount", 32);
        model.addAttribute("b2bCount", 10);
        model.addAttribute("collectionCentreCount", 6);
        model.addAttribute("pendingCollectionCount", 7);
        model.addAttribute("pendingTestingCount", 14);
        model.addAttribute("readyToPrintCount", 9);
        model.addAttribute("dispatchedCount", 18);

        model.addAttribute("allDepartments", Department.values());

        return "dashboard/index";
    }
}
