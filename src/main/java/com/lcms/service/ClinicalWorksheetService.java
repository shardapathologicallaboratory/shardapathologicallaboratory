package com.lcms.service;

import com.lcms.model.*;
import com.lcms.model.enums.Department;
import com.lcms.model.enums.OrderStatus;
import com.lcms.model.enums.ResultFlag;
import com.lcms.model.enums.SampleStatus;
import com.lcms.repository.LabOrderRepository;
import com.lcms.repository.LabSampleRepository;
import com.lcms.repository.RecheckFlagRepository;
import com.lcms.repository.TestResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClinicalWorksheetService {

    private final TestResultRepository resultRepository;
    private final LabOrderRepository orderRepository;
    private final LabSampleRepository sampleRepository;
    private final RecheckFlagRepository flagRepository;

    public ClinicalWorksheetService(TestResultRepository resultRepository,
                                    LabOrderRepository orderRepository,
                                    LabSampleRepository sampleRepository,
                                    RecheckFlagRepository flagRepository) {
        this.resultRepository = resultRepository;
        this.orderRepository = orderRepository;
        this.sampleRepository = sampleRepository;
        this.flagRepository = flagRepository;
    }

    public List<TestResult> getResultsForOrder(Long orderId) {
        return resultRepository.findByOrderIdOrderByDepartmentAscSortOrderAsc(orderId);
    }

    public List<RecheckFlag> getFlagsForOrder(Long orderId) {
        return flagRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    public List<RecheckFlag> getUnresolvedFlagsForDepartment(Department department) {
        return flagRepository.findByTargetDepartmentAndResolvedFalseOrderByCreatedAtDesc(department);
    }

    @Transactional
    public List<TestResult> getOrInitializeResults(LabOrder order) {
        List<TestResult> existing = resultRepository.findByOrderIdOrderByDepartmentAscSortOrderAsc(order.getId());
        if (!existing.isEmpty()) {
            return existing;
        }

        List<TestResult> created = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            TestItem testItem = item.getTestItem();
            if (testItem != null && testItem.getParameters() != null && !testItem.getParameters().isEmpty()) {
                for (TestParameter p : testItem.getParameters()) {
                    TestResult res = new TestResult(
                            item, order, order.getPatient(), item.getDepartment(),
                            p.getName(), p.getUnit(), p.getNormalRangeDisplay(),
                            p.getSortOrder(), p.isCalculated()
                    );
                    created.add(res);
                }
            } else {
                // Ad-hoc Custom Test (Req #15 / Unlisted)
                TestResult res = new TestResult(
                        item, order, order.getPatient(), item.getDepartment(),
                        item.getTestName(), "Obs", "Normal / As Observed",
                        1, false
                );
                created.add(res);
            }
        }
        return resultRepository.saveAll(created);
    }

    @Transactional
    public void saveWorksheetResults(
            Long orderId,
            Map<Long, String> values,
            Map<Long, String> comments,
            String enteredBy,
            Set<Department> allowedDepartments,
            boolean isLabAdmin) {

        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        List<TestResult> results = getOrInitializeResults(order);
        Map<String, Double> numericMap = new HashMap<>();

        // 1. Update entered values for allowed departments
        for (TestResult res : results) {
            boolean canEdit = isLabAdmin || allowedDepartments.contains(res.getDepartment());
            if (canEdit && values.containsKey(res.getId())) {
                String valStr = values.get(res.getId());
                res.setValueText(valStr != null ? valStr.trim() : "");
                res.setEnteredBy(enteredBy);
                res.setEnteredAt(LocalDateTime.now());

                if (comments != null && comments.containsKey(res.getId())) {
                    res.setTechnicalComment(comments.get(res.getId()));
                }

                // Parse numeric if applicable
                try {
                    if (valStr != null && !valStr.trim().isEmpty()) {
                        Double num = Double.parseDouble(valStr.trim());
                        res.setValueNumeric(num);
                        numericMap.put(res.getParameterName().toLowerCase(), num);
                    } else {
                        res.setValueNumeric(null);
                    }
                } catch (NumberFormatException ignored) {
                    res.setValueNumeric(null);
                }
            } else if (res.getValueNumeric() != null) {
                numericMap.put(res.getParameterName().toLowerCase(), res.getValueNumeric());
            }
        }

        // 2. Perform Automated CBC & Clinical Calculations (Point 18)
        performCalculations(results, numericMap);

        // 3. Evaluate Normal / Low / High / Panic Flags & Delta Checks
        for (TestResult res : results) {
            evaluateFlagsAndDelta(res);
        }

        resultRepository.saveAll(results);

        // Advance sample & order status if booked
        if (order.getStatus() == OrderStatus.BOOKED || order.getStatus() == OrderStatus.COLLECTED) {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
        }
    }

    private void performCalculations(List<TestResult> results, Map<String, Double> map) {
        // CBC Calculations
        Double hb = findValue(map, "hemoglobin");
        Double rbc = findValue(map, "rbc count");
        Double pcv = findValue(map, "pcv");
        Double tlc = findValue(map, "total leukocyte");

        // PCV = Hb * 3 if not manually entered
        if (hb != null && pcv == null) {
            pcv = Math.round(hb * 3.0 * 10.0) / 10.0;
            updateCalculatedResult(results, "pcv", pcv);
            map.put("pcv / hematocrit", pcv);
        }

        // MCV = (PCV * 10) / RBC
        if (pcv != null && rbc != null && rbc > 0) {
            double mcv = Math.round((pcv * 10.0 / rbc) * 10.0) / 10.0;
            updateCalculatedResult(results, "mcv", mcv);
        }

        // MCH = (Hb * 10) / RBC
        if (hb != null && rbc != null && rbc > 0) {
            double mch = Math.round((hb * 10.0 / rbc) * 10.0) / 10.0;
            updateCalculatedResult(results, "mch", mch);
        }

        // MCHC = (Hb * 100) / PCV
        if (hb != null && pcv != null && pcv > 0) {
            double mchc = Math.round((hb * 100.0 / pcv) * 10.0) / 10.0;
            updateCalculatedResult(results, "mchc", mchc);
        }

        // LFT Calculations: Indirect Bilirubin = Total - Direct
        Double totalBil = findValue(map, "total bilirubin");
        Double directBil = findValue(map, "direct bilirubin");
        if (totalBil != null && directBil != null && totalBil >= directBil) {
            double indBil = Math.round((totalBil - directBil) * 100.0) / 100.0;
            updateCalculatedResult(results, "indirect bilirubin", indBil);
        }

        // LFT: Globulin = Total Protein - Albumin
        Double protein = findValue(map, "total protein");
        Double albumin = findValue(map, "serum albumin");
        if (protein != null && albumin != null && protein >= albumin) {
            double globulin = Math.round((protein - albumin) * 10.0) / 10.0;
            updateCalculatedResult(results, "serum globulin", globulin);
            if (globulin > 0) {
                double agRatio = Math.round((albumin / globulin) * 100.0) / 100.0;
                updateCalculatedResult(results, "a/g ratio", agRatio);
            }
        }

        // Lipid: VLDL = TG / 5; LDL = Total Chol - HDL - VLDL
        Double chol = findValue(map, "total cholesterol");
        Double tg = findValue(map, "triglycerides");
        Double hdl = findValue(map, "hdl cholesterol");
        if (tg != null && tg > 0) {
            double vldl = Math.round((tg / 5.0) * 10.0) / 10.0;
            updateCalculatedResult(results, "vldl cholesterol", vldl);
            if (chol != null && hdl != null) {
                double ldl = Math.round((chol - hdl - vldl) * 10.0) / 10.0;
                updateCalculatedResult(results, "ldl cholesterol", ldl);
            }
        }
    }

    private Double findValue(Map<String, Double> map, String prefix) {
        for (var entry : map.entrySet()) {
            if (entry.getKey().contains(prefix)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private void updateCalculatedResult(List<TestResult> results, String key, Double val) {
        for (TestResult r : results) {
            if (r.getParameterName().toLowerCase().contains(key) && r.isCalculated()) {
                r.setValueNumeric(val);
                r.setValueText(String.valueOf(val));
                break;
            }
        }
    }

    private void evaluateFlagsAndDelta(TestResult res) {
        if (res.getValueNumeric() == null) {
            if (res.getValueText() != null && !res.getValueText().isEmpty()) {
                String v = res.getValueText().toLowerCase();
                if (v.contains("positive") || v.contains("reactive") || v.contains("present") || v.contains("1+") || v.contains("2+")) {
                    res.setFlag(ResultFlag.ABNORMAL);
                } else {
                    res.setFlag(ResultFlag.NORMAL);
                }
            }
            return;
        }

        double val = res.getValueNumeric();
        String range = res.getRangeSnapshot();

        // Parse range min & max (e.g. "12.0 - 15.0")
        if (range != null && range.contains("-")) {
            String[] parts = range.split("-");
            try {
                double min = Double.parseDouble(parts[0].trim());
                double max = Double.parseDouble(parts[1].trim());

                if (val < min * 0.7) {
                    res.setFlag(ResultFlag.CRITICAL_LOW);
                } else if (val < min) {
                    res.setFlag(ResultFlag.LOW);
                } else if (val > max * 1.5) {
                    res.setFlag(ResultFlag.CRITICAL_HIGH);
                } else if (val > max) {
                    res.setFlag(ResultFlag.HIGH);
                } else {
                    res.setFlag(ResultFlag.NORMAL);
                }
            } catch (Exception ignored) {
                res.setFlag(ResultFlag.NORMAL);
            }
        } else {
            res.setFlag(ResultFlag.NORMAL);
        }

        // Delta check comparison against previous verified results
        List<TestResult> past = resultRepository.findPastVerifiedResults(
                res.getPatient().getId(), res.getParameterName(), res.getId() != null ? res.getId() : -1L);

        if (!past.isEmpty()) {
            TestResult prev = past.get(0);
            if (prev.getValueNumeric() != null && prev.getValueNumeric() > 0) {
                res.setPreviousValue(prev.getValueNumeric());
                double delta = Math.abs(val - prev.getValueNumeric()) / prev.getValueNumeric() * 100.0;
                res.setDeltaPercentage(Math.round(delta * 10.0) / 10.0);
                if (delta >= 25.0) {
                    res.setDeltaAlert(true);
                }
            }
        }
    }

    @Transactional
    public void raiseRecheckFlag(Long orderId, Department sourceDept, Department targetDept, String raisedBy, String reason) {
        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        RecheckFlag flag = new RecheckFlag(order, sourceDept, targetDept, raisedBy, reason);
        flagRepository.save(flag);

        // Mark samples in target dept as FLAGGED_RECHECK
        for (LabSample s : order.getSamples()) {
            if (s.getDepartment() == targetDept) {
                s.setStatus(SampleStatus.FLAGGED_RECHECK);
                s.setFlagReason("Recheck requested by " + sourceDept.getDisplayName() + ": " + reason);
                sampleRepository.save(s);
            }
        }
    }

    @Transactional
    public void resolveRecheckFlag(Long flagId, String resolvedBy, String note) {
        RecheckFlag flag = flagRepository.findById(flagId)
                .orElseThrow(() -> new IllegalArgumentException("Flag not found: " + flagId));
        flag.setResolved(true);
        flag.setResolvedBy(resolvedBy);
        flag.setResolutionNote(note);
        flag.setResolvedAt(LocalDateTime.now());
        flagRepository.save(flag);
    }

    @Transactional
    public void verifyAllResultsForOrder(Long orderId, String verifiedBy) {
        LabOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        List<TestResult> results = resultRepository.findByOrderIdOrderByDepartmentAscSortOrderAsc(orderId);
        LocalDateTime now = LocalDateTime.now();

        for (TestResult r : results) {
            r.setStatus("VERIFIED");
            r.setVerifiedBy(verifiedBy);
            r.setVerifiedAt(now);
        }
        resultRepository.saveAll(results);

        order.setStatus(OrderStatus.VERIFIED);
        orderRepository.save(order);
    }
}
