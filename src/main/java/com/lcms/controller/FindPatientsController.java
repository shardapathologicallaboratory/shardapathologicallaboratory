package com.lcms.controller;

import com.lcms.model.Doctor;
import com.lcms.model.LabOrder;
import com.lcms.model.Patient;
import com.lcms.model.enums.OrderStatus;
import com.lcms.model.enums.PatientSource;
import com.lcms.repository.DoctorRepository;
import com.lcms.service.OrderService;
import com.lcms.service.PatientService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
public class FindPatientsController {

    private final OrderService orderService;
    private final PatientService patientService;
    private final DoctorRepository doctorRepository;

    public FindPatientsController(OrderService orderService, PatientService patientService, DoctorRepository doctorRepository) {
        this.orderService = orderService;
        this.patientService = patientService;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/patients/search")
    public String searchPatients(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "source", required = false) PatientSource source,
            @RequestParam(value = "doctorId", required = false) Long doctorId,
            @RequestParam(value = "status", required = false) OrderStatus status,
            Model model) {

        List<LabOrder> results = orderService.searchOrders(query, startDate, endDate, source, doctorId, status);
        List<Doctor> doctors = doctorRepository.findByActiveTrueOrderByNameAsc();

        model.addAttribute("orders", results);
        model.addAttribute("doctors", doctors);
        model.addAttribute("sources", PatientSource.values());
        model.addAttribute("statuses", OrderStatus.values());

        // Echo current filters
        model.addAttribute("query", query);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedSource", source);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("selectedStatus", status);

        return "patients/search";
    }

    @GetMapping("/patients/{id}")
    public String patientHistory(@PathVariable("id") Long id, Model model) {
        Patient patient = patientService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));

        List<LabOrder> pastOrders = orderService.getOrdersForPatient(id);

        model.addAttribute("patient", patient);
        model.addAttribute("orders", pastOrders);

        return "patients/history";
    }
}
