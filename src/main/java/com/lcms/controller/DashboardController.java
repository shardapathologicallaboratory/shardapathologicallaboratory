package com.lcms.controller;

import com.lcms.model.User;
import com.lcms.model.enums.Department;
import com.lcms.model.enums.OrderStatus;
import com.lcms.model.enums.PatientSource;
import com.lcms.model.enums.SampleStatus;
import com.lcms.repository.LabOrderRepository;
import com.lcms.repository.LabSampleRepository;
import com.lcms.repository.PatientRepository;
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
    private final LabOrderRepository orderRepository;
    private final PatientRepository patientRepository;
    private final LabSampleRepository sampleRepository;

    public DashboardController(UserService userService, LabOrderRepository orderRepository,
                               PatientRepository patientRepository, LabSampleRepository sampleRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.patientRepository = patientRepository;
        this.sampleRepository = sampleRepository;
    }

    @GetMapping("/")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername()).orElse(userDetails.getUser());
        model.addAttribute("currentUser", user);
        
        LocalDate today = LocalDate.now();
        model.addAttribute("currentDate", today.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        
        // Real database operational counts
        long totalOrdersToday = orderRepository.countByOrderDate(today);
        long walkInToday = orderRepository.countByOrderDateAndSource(today, PatientSource.WALK_IN);
        long b2bToday = orderRepository.countByOrderDateAndSource(today, PatientSource.B2B);
        long ccToday = orderRepository.countByOrderDateAndSource(today, PatientSource.COLLECTION_CENTRE);

        long pendingCollection = sampleRepository.countByStatus(SampleStatus.PENDING_COLLECTION);
        long inTesting = orderRepository.countByStatus(OrderStatus.PROCESSING) + orderRepository.countByStatus(OrderStatus.COLLECTED);
        long readyToPrint = orderRepository.countByStatus(OrderStatus.VERIFIED);
        long dispatched = orderRepository.countByStatus(OrderStatus.DISPATCHED);

        model.addAttribute("totalPatientsToday", totalOrdersToday > 0 ? totalOrdersToday : patientRepository.countTotalPatients());
        model.addAttribute("walkInCount", walkInToday);
        model.addAttribute("b2bCount", b2bToday);
        model.addAttribute("collectionCentreCount", ccToday);
        model.addAttribute("pendingCollectionCount", pendingCollection);
        model.addAttribute("pendingTestingCount", inTesting);
        model.addAttribute("readyToPrintCount", readyToPrint);
        model.addAttribute("dispatchedCount", dispatched);

        model.addAttribute("allDepartments", Department.values());

        return "dashboard/index";
    }
}
