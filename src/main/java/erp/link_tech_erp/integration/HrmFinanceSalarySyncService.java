package erp.link_tech_erp.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import erp.link_tech_erp.finance.model.FinancialRecord;
import erp.link_tech_erp.finance.model.RecordType;
import erp.link_tech_erp.finance.repository.ApprovalRequestRepository;
import erp.link_tech_erp.finance.repository.AuditLogRepository;
import erp.link_tech_erp.finance.repository.FinancialRecordRepository;
import erp.link_tech_erp.finance.repository.PeriodLockRepository;
import erp.link_tech_erp.finance.repository.SupabaseRestConfig;
import erp.link_tech_erp.finance.repository.UserRoleRepository;
import erp.link_tech_erp.finance.service.ComplianceService;
import erp.link_tech_erp.finance.service.FinanceService;
import erp.link_tech_erp.hrm.DatabaseConnection;

public final class HrmFinanceSalarySyncService {
    private static final String PAYROLL_CATEGORY = "Payroll";

    private final FinanceService financeService;

    private HrmFinanceSalarySyncService(FinanceService financeService) {
        this.financeService = financeService;
    }

    public static HrmFinanceSalarySyncService createDefault() {
        SupabaseRestConfig restConfig = SupabaseRestConfig.fromEnvironment();

        UserRoleRepository userRoleRepository = new UserRoleRepository(restConfig);
        PeriodLockRepository periodLockRepository = new PeriodLockRepository(restConfig);
        ApprovalRequestRepository approvalRequestRepository = new ApprovalRequestRepository(restConfig);
        AuditLogRepository auditLogRepository = new AuditLogRepository(restConfig);
        FinancialRecordRepository financialRecordRepository = new FinancialRecordRepository(restConfig);

        ComplianceService complianceService = new ComplianceService(
            userRoleRepository,
            periodLockRepository,
            approvalRequestRepository,
            auditLogRepository
        );

        return new HrmFinanceSalarySyncService(new FinanceService(financialRecordRepository, complianceService));
    }

    public FinancialRecord syncEmployeeSalary(String employeeId) {
        String[] employee = DatabaseConnection.getEmployeeById(employeeId);
        if (employee == null || employee.length < 5) {
            throw new IllegalStateException("Employee not found for payroll sync.");
        }

        BigDecimal salary = parseSalary(employee[4]);
        LocalDate effectiveDate = LocalDate.now();
        String recordId = buildPayrollRecordId(employee[0], YearMonth.from(effectiveDate));
        String description = buildDescription(employee);

        FinancialRecord record = new FinancialRecord(
            recordId,
            effectiveDate,
            RecordType.EXPENSE,
            PAYROLL_CATEGORY,
            description,
            salary.doubleValue()
        );

        return financeService.upsertRecord(record);
    }

    private static BigDecimal parseSalary(String salaryText) {
        if (salaryText == null || salaryText.isBlank()) {
            throw new IllegalStateException("Employee salary is missing.");
        }

        String normalized = salaryText.trim().replace(",", "");
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Employee salary is not a valid number: " + salaryText, exception);
        }
    }

    private static String buildPayrollRecordId(String employeeId, YearMonth period) {
        return "hrm-payroll:" + period + ":" + employeeId;
    }

    private static String buildDescription(String[] employee) {
        String employeeId = valueAt(employee, 0);
        String name = valueAt(employee, 1);
        String position = valueAt(employee, 2);
        String department = valueAt(employee, 3);

        return "Payroll expense for " + safeLabel(name) + " (" + safeLabel(employeeId) + ")"
                + (department.isBlank() ? "" : " - " + department)
                + (position.isBlank() ? "" : " / " + position);
    }

    private static String valueAt(String[] values, int index) {
        if (values == null || index < 0 || index >= values.length || values[index] == null) {
            return "";
        }
        return values[index].trim();
    }

    private static String safeLabel(String value) {
        return value == null || value.isBlank() ? "Unknown" : value.trim();
    }
}