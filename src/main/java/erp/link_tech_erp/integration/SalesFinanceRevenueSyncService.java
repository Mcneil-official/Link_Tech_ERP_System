package erp.link_tech_erp.integration;

import java.sql.Timestamp;
import java.time.LocalDate;

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
import erp.link_tech_erp.sales.SalesOrder;
import erp.link_tech_erp.sales.SalesOrderRepository;

public final class SalesFinanceRevenueSyncService {

    private static final String SALES_REVENUE_CATEGORY = "Sales Revenue";

    private final SalesOrderRepository salesOrderRepository;
    private final FinanceService financeService;

    private SalesFinanceRevenueSyncService(SalesOrderRepository salesOrderRepository, FinanceService financeService) {
        this.salesOrderRepository = salesOrderRepository;
        this.financeService = financeService;
    }

    public static SalesFinanceRevenueSyncService createDefault() {
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

        return new SalesFinanceRevenueSyncService(
            new SalesOrderRepository(),
            new FinanceService(financialRecordRepository, complianceService)
        );
    }

    public FinancialRecord syncPaidOrderRevenue(int orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalStateException("Sales order not found for finance sync: " + orderId));

        if (!"Paid".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new IllegalStateException("Sales order is not paid and cannot be synced to finance: " + orderId);
        }

        if (order.getTotalPrice() <= 0.0) {
            throw new IllegalStateException("Sales order total price must be greater than zero: " + orderId);
        }

        FinancialRecord record = new FinancialRecord(
            buildRevenueRecordId(order),
            resolveDate(order.getOrderDate()),
            RecordType.INCOME,
            SALES_REVENUE_CATEGORY,
            buildDescription(order),
            order.getTotalPrice()
        );

        return financeService.upsertRecord(record);
    }

    private static String buildRevenueRecordId(SalesOrder order) {
        return "sales-revenue:" + order.getOrderId();
    }

    private static LocalDate resolveDate(Timestamp orderDate) {
        if (orderDate == null) {
            return LocalDate.now();
        }
        return orderDate.toLocalDateTime().toLocalDate();
    }

    private static String buildDescription(SalesOrder order) {
        String customer = order.getCustomerName() == null || order.getCustomerName().isBlank()
            ? "Unknown customer"
            : order.getCustomerName().trim();
        String product = order.getProductName() == null || order.getProductName().isBlank()
            ? "Unknown product"
            : order.getProductName().trim();

        return "Revenue from sales order #" + order.getOrderId() + " - " + customer + " / " + product;
    }
}