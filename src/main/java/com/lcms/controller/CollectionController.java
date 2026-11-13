package com.lcms.controller;

import com.lcms.model.LabOrder;
import com.lcms.model.LabSample;
import com.lcms.model.OrderItem;
import com.lcms.model.enums.Department;
import com.lcms.security.CustomUserDetails;
import com.lcms.service.BarcodeService;
import com.lcms.service.CollectionService;
import com.lcms.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/collection")
@PreAuthorize("hasAnyRole('ADMIN', 'COLLECTION', 'RECEPTION')")
public class CollectionController {

    private final CollectionService collectionService;
    private final OrderService orderService;
    private final BarcodeService barcodeService;

    public CollectionController(CollectionService collectionService, OrderService orderService, BarcodeService barcodeService) {
        this.collectionService = collectionService;
        this.orderService = orderService;
        this.barcodeService = barcodeService;
    }

    @GetMapping
    public String collectionDashboard(Model model) {
        List<LabSample> pendingSamples = collectionService.getPendingSamples();
        List<LabSample> recollectionAlerts = collectionService.getRecollectionAlerts();

        model.addAttribute("pendingSamples", pendingSamples);
        model.addAttribute("recollectionAlerts", recollectionAlerts);
        model.addAttribute("departments", Department.values());

        return "collection/dashboard";
    }

    @PostMapping("/samples/{id}/collect")
    public String collectSample(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            LabSample sample = collectionService.markSampleCollected(id, userDetails.getFullName());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Sample #" + sample.getSampleSequenceId() + " (" + sample.getVialType().getDisplayName() + ") marked as COLLECTED!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/collection";
    }

    @PostMapping("/orders/{id}/collect-all")
    public String collectAllSamplesForOrder(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            collectionService.markAllSamplesCollectedForOrder(id, userDetails.getFullName());
            redirectAttributes.addFlashAttribute("successMessage", "All sample vials for order have been marked as COLLECTED!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/collection";
    }

    @GetMapping("/samples/{id}/barcode")
    public String viewBarcodeSticker(@PathVariable("id") Long id, Model model) {
        LabSample sample = collectionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sample not found: " + id));

        LabOrder order = sample.getOrder();
        String barcodeData = sample.getBarcode();
        String barcodeImageBase64 = barcodeService.generateCode128Base64(barcodeData, 220, 60);

        List<String> testsForSample = order.getItems().stream()
                .filter(i -> i.getDepartment() == sample.getDepartment())
                .map(OrderItem::getTestName)
                .collect(Collectors.toList());

        model.addAttribute("sample", sample);
        model.addAttribute("order", order);
        model.addAttribute("patient", order.getPatient());
        model.addAttribute("barcodeBase64", barcodeImageBase64);
        model.addAttribute("testsSummary", String.join(", ", testsForSample));

        return "collection/barcode-sticker";
    }

    @GetMapping("/lab-sheet")
    public String labRunSheet(
            @RequestParam(value = "department", defaultValue = "HEMATOLOGY") Department department,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime startTime = targetDate.atStartOfDay();
        LocalDateTime endTime = targetDate.atTime(LocalTime.MAX);

        var rows = collectionService.generateLabRunSheet(department, startTime, endTime);

        model.addAttribute("department", department);
        model.addAttribute("targetDate", targetDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        model.addAttribute("startTimeStr", targetDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + " 00:00:00");
        model.addAttribute("endTimeStr", targetDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + " 23:59:59");
        model.addAttribute("rows", rows);
        model.addAttribute("departments", Department.values());

        return "collection/lab-sheet";
    }
}
