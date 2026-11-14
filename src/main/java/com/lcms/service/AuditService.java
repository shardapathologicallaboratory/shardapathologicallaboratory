package com.lcms.service;

import com.lcms.model.AuditLog;
import com.lcms.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class AuditService {

    public static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Records an immutable entry in the cryptographic audit chain.
     * Prev_hash links to the latest block, creating a tamper-evident sequence.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized AuditLog logAction(String actor, String action, String resourceType, String resourceId, String details, String ipAddress) {
        String prevHash = auditLogRepository.findTopByOrderByIdDesc()
                .map(AuditLog::getCurrentHash)
                .orElse(GENESIS_HASH);

        String payload = String.format("%s|%s|%s|%s|%s|%s", prevHash, actor, action, resourceType, resourceId, details != null ? details : "");
        String currentHash = computeSha256(payload);

        AuditLog log = new AuditLog(actor, action, resourceType, resourceId, details, prevHash, currentHash, ipAddress);
        return auditLogRepository.save(log);
    }

    /**
     * Iterates through the entire cryptographic chain from genesis to head.
     * Recalculates each block's SHA-256 hash and validates link to previous block.
     */
    @Transactional(readOnly = true)
    public AuditIntegrityResult verifyChainIntegrity() {
        List<AuditLog> allLogs = auditLogRepository.findAllByOrderByIdAsc();
        if (allLogs.isEmpty()) {
            return new AuditIntegrityResult(true, 0, "Audit trail is empty. Ready for new records.");
        }

        String expectedPrevHash = GENESIS_HASH;
        for (int i = 0; i < allLogs.size(); i++) {
            AuditLog log = allLogs.get(i);

            // 1. Check previous hash continuity
            if (!log.getPreviousHash().equals(expectedPrevHash)) {
                return new AuditIntegrityResult(false, i + 1,
                        "Chain broken at entry #" + log.getId() + "! Previous hash mismatch.");
            }

            // 2. Check current hash authenticity
            String payload = String.format("%s|%s|%s|%s|%s|%s",
                    log.getPreviousHash(), log.getActorUsername(), log.getAction(),
                    log.getResourceType(), log.getResourceId(),
                    log.getDetailsJson() != null ? log.getDetailsJson() : "");
            String calculatedHash = computeSha256(payload);

            if (!calculatedHash.equals(log.getCurrentHash())) {
                return new AuditIntegrityResult(false, i + 1,
                        "Tampering detected at entry #" + log.getId() + "! Calculated hash does not match stored hash.");
            }

            expectedPrevHash = log.getCurrentHash();
        }

        return new AuditIntegrityResult(true, allLogs.size(),
                "All " + allLogs.size() + " audit blocks verified. Chain is 100% cryptographic tamper-proof.");
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop200ByOrderByIdDesc();
    }

    public static String computeSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    public record AuditIntegrityResult(boolean valid, int verifiedEntries, String message) {}
}
