package com.lcms.controller;

import com.lcms.model.Doctor;
import com.lcms.model.LabOrder;
import com.lcms.model.Patient;
import com.lcms.model.ShiftSession;
import com.lcms.model.TestItem;
import com.lcms.model.enums.*;
import com.lcms.repository.DoctorRepository;
import com.lcms.security.CustomUserDetails;
import com.lcms.service.OrderService;
import com.lcms.service.PatientService;
import com.lcms.service.ShiftService;
import com.lcms.service.TestCatalogueService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reception")
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
public class ReceptionController {

    private final PatientService patientService;
    private final OrderService orderService;
    private final TestCatalogueService catalogueService;
    private final DoctorRepository doctorRepository;
    private final ShiftService shiftService;

    public ReceptionController(PatientService patientService, OrderService orderService,
                               TestCatalogueService catalogueService, DoctorRepository doctorRepository,
                               ShiftService shiftService) {
        this.patientService = patientService;
        this.orderService = orderService;
        this.catalogueService = catalogueService;
        this.doctorRepository = doctorRepository;
        this.shiftService = shiftService;
    }

    @GetMapping
    public String receptionDashboard(Model model) {
        LocalDate today = LocalDate.now();
        List<LabOrder> todayOrders = orderService.searchOrders(null, today, today, null, null, null);
        
        long walkInCount = todayOrders.stream().filter(o -> o.getSource() == PatientSource.WALK_IN).count();
        long b2bCount = todayOrders.stream().filter(o -> o.getSource() == PatientSource.B2B).count();
        long ccCount = todayOrders.stream().filter(o -> o.getSource() == PatientSource.COLLECTION_CENTRE).count();

        model.addAttribute("todayOrders", todayOrders);
        model.addAttribute("totalToday", todayOrders.size());
        model.addAttribute("walkInCount", walkInCount);
        model.addAttribute("b2bCount", b2bCount);
        model.addAttribute("ccCount", ccCount);

        return "reception/dashboard";
    }

    @GetMapping("/new-registration")
    public String newRegistrationPage(
            @RequestParam(value = "existingPatientId", required = false) Long existingPatientId,
            Model model) {

        if (existingPatientId != null) {
            patientService.findById(existingPatientId).ifPresent(p -> model.addAttribute("existingPatient", p));
        }

        model.addAttribute("doctors", doctorRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("tests", catalogueService.getAllActiveTests());
        model.addAttribute("departments", Department.values());
        model.addAttribute("vialTypes", VialType.values());
        model.addAttribute("sources", PatientSource.values());
        model.addAttribute("paymentModes", PaymentMode.values());
        model.addAttribute("genders", Gender.values());

        return "reception/registration";
    }

    @PostMapping("/orders/create")
    public String createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "patientId", required = false) Long patientId,
            // New patient fields if not existing
            @RequestParam(value = "salutation", defaultValue = "Mr.") String salutation,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "ageYears", defaultValue = "0") Integer ageYears,
            @RequestParam(value = "ageMonths", defaultValue = "0") Integer ageMonths,
            @RequestParam(value = "gender", defaultValue = "MALE") Gender gender,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "bloodGroup", required = false) String bloodGroup,
            // Visit details
            @RequestParam("doctorId") Long doctorId,
            @RequestParam(value = "source", defaultValue = "WALK_IN") PatientSource source,
            @RequestParam(value = "centreCode", defaultValue = "MAIN") String centreCode,
            @RequestParam(value = "selectedTestIds", required = false) List<Long> selectedTestIds,
            // Ad-hoc Custom Test Fields
            @RequestParam(value = "customTestName", required = false) String customTestName,
            @RequestParam(value = "customDepartment", required = false) Department customDepartment,
            @RequestParam(value = "customVialType", required = false) VialType customVialType,
            @RequestParam(value = "customPrice", required = false) BigDecimal customPrice,
            // Billing fields
            @RequestParam(value = "discountAmount", defaultValue = "0") BigDecimal discountAmount,
            @RequestParam(value = "discountReason", required = false) String discountReason,
            @RequestParam(value = "paidAmount", defaultValue = "0") BigDecimal paidAmount,
            @RequestParam(value = "paymentMode", defaultValue = "CASH") PaymentMode paymentMode,
            @RequestParam(value = "paymentReference", required = false) String paymentReference,
            @RequestParam(value = "clinicalNotes", required = false) String clinicalNotes,
            RedirectAttributes redirectAttributes) {

        try {
            Patient patient;
            if (patientId != null) {
                patient = patientService.findById(patientId)
                        .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
            } else {
                if (fullName == null || fullName.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
                    throw new IllegalArgumentException("Patient Name and Phone number are mandatory.");
                }
                patient = new Patient(null, salutation, fullName.trim(), ageYears, ageMonths, gender, phone.trim(), address);
                patient.setBloodGroup(bloodGroup);
                patient = patientService.registerPatient(patient);
            }

            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

            List<OrderService.CustomTestRequest> customTests = new ArrayList<>();
            if (customTestName != null && !customTestName.trim().isEmpty() && customDepartment != null && customVialType != null) {
                customTests.add(new OrderService.CustomTestRequest(customTestName.trim(), customDepartment, customVialType, customPrice));
            }

            if ((selectedTestIds == null || selectedTestIds.isEmpty()) && customTests.isEmpty()) {
                throw new IllegalArgumentException("Please select at least one test to book an order.");
            }

            // Auto-start Reception Shift if not already open for this user
            String autoShiftStartedMsg = null;
            if (userDetails != null && userDetails.getUser() != null) {
                if (shiftService.getCurrentShift(userDetails.getUser().getId()).isEmpty()) {
                    ShiftSession newShift = shiftService.openShift(userDetails.getUser(), BigDecimal.ZERO);
                    autoShiftStartedMsg = "Shift #" + newShift.getSessionCode() + " has automatically started for your counter! All billings and receipts are actively tracked under your name.";
                }
            }

            LabOrder order = orderService.createOrder(
                    patient.getId(), doctor, source, centreCode,
                    selectedTestIds, customTests,
                    discountAmount, discountReason, paidAmount, paymentMode, paymentReference,
                    userDetails.getFullName(), clinicalNotes
            );

            if (autoShiftStartedMsg != null) {
                redirectAttributes.addFlashAttribute("shiftNotification", autoShiftStartedMsg);
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Patient booked successfully! Token #" + order.getDailyTokenNumber() + " | Order ID: " + order.getOrderNumber() +
                    " (" + order.getSamples().size() + " sample vials queued for phlebotomy)");

            return "redirect:/reception";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking failed: " + ex.getMessage());
            return "redirect:/reception/new-registration";
        }
    }

    @PostMapping("/patients/{id}/update-demographics")
    public String updatePatientDemographics(
            @PathVariable("id") Long id,
            @RequestParam("salutation") String salutation,
            @RequestParam("fullName") String fullName,
            @RequestParam("ageYears") Integer ageYears,
            @RequestParam("ageMonths") Integer ageMonths,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("bloodGroup") String bloodGroup,
            RedirectAttributes redirectAttributes) {

        try {
            patientService.updateDemographics(id, salutation, fullName, ageYears, ageMonths, phone, address, bloodGroup);
            redirectAttributes.addFlashAttribute("successMessage", "Patient demographic details updated securely.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/patients/" + id;
    }

    @PostMapping("/orders/{id}/dispatch")
    public String dispatchOrder(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "dispatchedTo", defaultValue = "Patient") String dispatchedTo,
            RedirectAttributes redirectAttributes) {

        try {
            orderService.markDispatched(id, userDetails.getFullName(), dispatchedTo);
            redirectAttributes.addFlashAttribute("successMessage", "Report marked as DISPATCHED to " + dispatchedTo);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/reception";
    }

    @PostMapping("/doctors/quick-add")
    public String quickAddDoctor(
            @RequestParam("name") String name,
            @RequestParam("qualification") String qualification,
            @RequestParam("clinicOrHospital") String clinicOrHospital,
            @RequestParam("phone") String phone,
            RedirectAttributes redirectAttributes) {

        try {
            Doctor doc = new Doctor(name.trim(), qualification, clinicOrHospital, phone, "");
            doctorRepository.save(doc);
            redirectAttributes.addFlashAttribute("successMessage", "Doctor " + name + " added successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/reception/new-registration";
    }
}
