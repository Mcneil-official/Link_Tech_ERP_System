package erp.link_tech_erp.integration;

import java.time.LocalDate;

import erp.link_tech_erp.finance.model.Bill;
import erp.link_tech_erp.finance.model.FinancialRecord;
import erp.link_tech_erp.finance.model.Payment;
import erp.link_tech_erp.finance.model.PaymentType;
import erp.link_tech_erp.finance.model.RecordType;
import erp.link_tech_erp.finance.repository.ApprovalRequestRepository;
import erp.link_tech_erp.finance.repository.AuditLogRepository;
import erp.link_tech_erp.finance.repository.FinancialRecordRepository;
import erp.link_tech_erp.finance.repository.PeriodLockRepository;
import erp.link_tech_erp.finance.repository.SupabaseRestConfig;
import erp.link_tech_erp.finance.repository.UserRoleRepository;
import erp.link_tech_erp.finance.service.ComplianceService;
import erp.link_tech_erp.finance.service.FinanceService;

public final class ProcurementFinanceExpenseSyncService {

    private static final String PROCUREMENT_COMMITTED_COST_CATEGORY = "Procurement Committed Cost";
    private static final String SUPPLIER_PAYMENT_CATEGORY = "Supplier Payment";

    private final FinanceService financeService;

    private ProcurementFinanceExpenseSyncService(FinanceService financeService) {
        this.financeService = financeService;
    }

    public static ProcurementFinanceExpenseSyncService createDefault() {
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

        return new ProcurementFinanceExpenseSyncService(
            new FinanceService(financialRecordRepository, complianceService)
        );
    }

    public FinancialRecord syncConfirmedPurchaseOrder(
            int orderId,
            String supplierName,
            int quantity,
            double unitPrice,
            LocalDate orderDate) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Purchase order id is required.");
        }
        if (quantity <= 0 || unitPrice <= 0.0) {
            throw new IllegalArgumentException("Purchase order quantity and unit price must be greater than zero.");
        }

        double totalAmount = quantity * unitPrice;
        String safeSupplier = supplierName == null || supplierName.isBlank() ? "Unknown supplier" : supplierName.trim();
        LocalDate effectiveDate = orderDate == null ? LocalDate.now() : orderDate;

        FinancialRecord record = new FinancialRecord(
            "procurement-po-confirmed:" + orderId,
            effectiveDate,
            RecordType.EXPENSE,
            PROCUREMENT_COMMITTED_COST_CATEGORY,
            "Committed cost for PO #" + orderId + " - " + safeSupplier + " (" + quantity + " x " + unitPrice + ")",
            totalAmount
        );

        return financeService.upsertRecord(record);
    }

    public FinancialRecord syncSupplierDisbursement(Bill bill, Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment is required for supplier disbursement sync.");
        }
        if (payment.getPaymentType() != PaymentType.DISBURSEMENT) {
            throw new IllegalArgumentException("Only DISBURSEMENT payments can be synced as supplier payments.");
        }
        if (payment.getId() == null || payment.getId().isBlank()) {
            throw new IllegalArgumentException("Payment id is required for supplier disbursement sync.");
        }
        if (payment.getAmount() <= 0.0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        LocalDate effectiveDate = payment.getPaymentDate() == null ? LocalDate.now() : payment.getPaymentDate();
        String billNo = bill == null || bill.getBillNo() == null || bill.getBillNo().isBlank() ? "N/A" : bill.getBillNo().trim();
        String vendorId = bill == null || bill.getVendorId() == null || bill.getVendorId().isBlank() ? "Unknown vendor" : bill.getVendorId().trim();
        String billId = payment.getBillId() == null || payment.getBillId().isBlank() ? "N/A" : payment.getBillId().trim();

        FinancialRecord record = new FinancialRecord(
            "procurement-disbursement:" + payment.getId(),
            effectiveDate,
            RecordType.EXPENSE,
            SUPPLIER_PAYMENT_CATEGORY,
            "Supplier payment " + payment.getPaymentNo() + " for bill " + billNo + " (billId=" + billId + ", vendor=" + vendorId + ")",
            payment.getAmount()
        );

        return financeService.upsertRecord(record);
    }
}