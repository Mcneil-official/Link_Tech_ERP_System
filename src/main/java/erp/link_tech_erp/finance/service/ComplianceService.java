package erp.link_tech_erp.finance.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import erp.link_tech_erp.finance.repository.ApprovalRequestRepository;
import erp.link_tech_erp.finance.repository.AuditLogRepository;
import erp.link_tech_erp.finance.repository.PeriodLockRepository;
import erp.link_tech_erp.finance.repository.UserRoleRepository;

public class ComplianceService {
    private static final double APPROVAL_THRESHOLD = 100000.0;

    private final UserRoleRepository userRoleRepository;
    private final PeriodLockRepository periodLockRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final AuditLogRepository auditLogRepository;

    public ComplianceService(UserRoleRepository userRoleRepository,
                             PeriodLockRepository periodLockRepository,
                             ApprovalRequestRepository approvalRequestRepository,
                             AuditLogRepository auditLogRepository) {
        this.userRoleRepository = userRoleRepository;
        this.periodLockRepository = periodLockRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public String getCurrentUserEmail() {
        String email = CurrentUserContext.getCurrentEmail();
        return (email == null || email.isBlank()) ? "system" : email;
    }

    public String getCurrentRole() {
        String email = CurrentUserContext.getCurrentEmail();
        if (email == null || email.isBlank()) {
            return "ADMIN";
        }
        Optional<String> role = userRoleRepository.findRoleByEmail(email);
        return role.orElse("ACCOUNTANT").toUpperCase(Locale.ROOT);
    }

    public void assertCanMutate(String action, LocalDate effectiveDate, double amount, String sourceId, String reason) {
        String role = getCurrentRole();
        String actor = getCurrentUserEmail();

        if ("VIEWER".equals(role)) {
            throw new IllegalStateException("Access denied. Viewer role cannot perform write operations.");
        }

        if (effectiveDate != null && periodLockRepository.isDateLocked(effectiveDate)) {
            throw new IllegalStateException("The accounting period for " + effectiveDate + " is locked.");
        }

        if (Math.abs(amount) >= APPROVAL_THRESHOLD && !("ADMIN".equals(role) || "APPROVER".equals(role))) {
            approvalRequestRepository.createPending(action, sourceId, actor,
                    "Auto-generated approval request for amount " + String.format("%.2f", amount)
                            + (reason == null || reason.isBlank() ? "" : ". Reason: " + reason));
            throw new IllegalStateException("Approval required for high-value transaction (>= "
                    + String.format("%.2f", APPROVAL_THRESHOLD) + "). A pending request was created.");
        }
    }

    public List<ApprovalRequestView> getPendingApprovals(int limit) {
        JsonNode rows = approvalRequestRepository.findPending(limit);
        List<ApprovalRequestView> result = new ArrayList<>();
        for (JsonNode row : rows) {
            result.add(new ApprovalRequestView(
                    row.path("id").asText(""),
                    row.path("request_type").asText(""),
                    row.path("source_id").asText(""),
                    row.path("requested_by").asText(""),
                    row.path("status").asText(""),
                    row.path("requested_at").asText(""),
                    row.path("remarks").asText("")));
        }
        return result;
    }

    public boolean resolveApprovalRequest(String requestId, boolean approved, String remarks) {
        assertCanManageCompliance();
        String status = approved ? "APPROVED" : "REJECTED";
        boolean updated = approvalRequestRepository.updateStatus(
                requestId,
                status,
                getCurrentUserEmail(),
                remarks);
        if (updated) {
            logAudit(
                    "approval_requests",
                    requestId,
                    status,
                    null,
                    "{\"status\":\"" + status + "\"}",
                    remarks == null || remarks.isBlank() ? "Approval decision recorded" : remarks);
        }
        return updated;
    }

    public List<PeriodLockView> getRecentPeriodLocks(int limit) {
        JsonNode rows = periodLockRepository.findRecent(limit);
        List<PeriodLockView> result = new ArrayList<>();
        for (JsonNode row : rows) {
            result.add(new PeriodLockView(
                    row.path("id").asText(""),
                    row.path("period_start").asText(""),
                    row.path("period_end").asText(""),
                    row.path("is_locked").asBoolean(false),
                    row.path("locked_by").asText(""),
                    row.path("locked_at").asText("")));
        }
        return result;
    }

    public boolean setPeriodLock(LocalDate startDate, LocalDate endDate, boolean isLocked, String remarks) {
        assertCanManageCompliance();
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Period start and end dates are required.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Period end date cannot be earlier than start date.");
        }

        boolean changed = periodLockRepository.upsertLock(startDate, endDate, isLocked, getCurrentUserEmail());
        if (changed) {
            logAudit(
                    "period_locks",
                    startDate + "_" + endDate,
                    isLocked ? "LOCK" : "UNLOCK",
                    null,
                    "{\"period_start\":\"" + startDate + "\",\"period_end\":\"" + endDate
                            + "\",\"is_locked\":" + isLocked + "}",
                    remarks == null || remarks.isBlank() ? "Period lock updated" : remarks);
        }
        return changed;
    }

    public List<AuditLogView> getRecentAuditLogs(int limit) {
        JsonNode rows = auditLogRepository.findRecent(limit);
        List<AuditLogView> result = new ArrayList<>();
        for (JsonNode row : rows) {
            result.add(new AuditLogView(
                    row.path("id").asText(""),
                    row.path("entity_name").asText(""),
                    row.path("entity_id").asText(""),
                    row.path("action").asText(""),
                    row.path("changed_by").asText(""),
                    row.path("changed_at").asText(""),
                    row.path("reason").asText("")));
        }
        return result;
    }

    public void logAudit(String entityName,
                         String entityId,
                         String action,
                         String oldDataJson,
                         String newDataJson,
                         String reason) {
        auditLogRepository.createLog(
                entityName,
                entityId,
                action,
                getCurrentUserEmail(),
                oldDataJson,
                newDataJson,
                reason);
    }

    private void assertCanManageCompliance() {
        String role = getCurrentRole();
        if (!("ADMIN".equals(role) || "APPROVER".equals(role))) {
            throw new IllegalStateException("Access denied. Only ADMIN or APPROVER can manage compliance workflows.");
        }
    }

    public record ApprovalRequestView(
            String id,
            String requestType,
            String sourceId,
            String requestedBy,
            String status,
            String requestedAt,
            String remarks) {
    }

    public record PeriodLockView(
            String id,
            String periodStart,
            String periodEnd,
            boolean locked,
            String lockedBy,
            String lockedAt) {
    }

    public record AuditLogView(
            String id,
            String entityName,
            String entityId,
            String action,
            String changedBy,
            String changedAt,
            String reason) {
    }
}
