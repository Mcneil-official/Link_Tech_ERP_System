package erp.link_tech_erp.finance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import erp.link_tech_erp.finance.model.Bill;
import erp.link_tech_erp.finance.model.DocumentStatus;
import erp.link_tech_erp.finance.model.Payment;
import erp.link_tech_erp.finance.model.PaymentType;
import erp.link_tech_erp.finance.repository.BillRepository;
import erp.link_tech_erp.finance.repository.PaymentRepository;
import erp.link_tech_erp.integration.ProcurementFinanceExpenseSyncService;

public class AccountsPayableService {
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final ComplianceService complianceService;
    private ProcurementFinanceExpenseSyncService procurementFinanceExpenseSyncService;

    public AccountsPayableService(BillRepository billRepository,
                                  PaymentRepository paymentRepository,
                                  ComplianceService complianceService) {
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.complianceService = complianceService;
    }

    public Bill issueBill(Bill bill) {
        if (bill.getIssueDate() == null) {
            bill.setIssueDate(LocalDate.now());
        }
        if (bill.getDueDate() == null) {
            bill.setDueDate(bill.getIssueDate().plusDays(30));
        }
        if (bill.getStatus() == null || bill.getStatus() == DocumentStatus.DRAFT) {
            bill.setStatus(DocumentStatus.ISSUED);
        }

        double subtotal = bill.getSubtotal();
        if (subtotal <= 0.0 && bill.getLines() != null) {
            subtotal = bill.getLines().stream().mapToDouble(line -> line.getLineTotal()).sum();
            bill.setSubtotal(subtotal);
        }
        bill.setTotalAmount(bill.getSubtotal() + bill.getTaxAmount());
        complianceService.assertCanMutate("ISSUE_BILL", bill.getIssueDate(), bill.getTotalAmount(),
            bill.getBillNo(), "Issue bill");
        Bill created = billRepository.create(bill);
        complianceService.logAudit(
            "bills",
            created.getId(),
            "CREATE",
            null,
            toBillJson(created),
            "Bill issued");
        return created;
    }

    public boolean recordDisbursement(String billId, double amount, String method, String referenceNo) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        Optional<Bill> optional = billRepository.findById(billId);
        if (optional.isEmpty()) {
            return false;
        }

        Bill bill = optional.get();
        complianceService.assertCanMutate("RECORD_DISBURSEMENT", LocalDate.now(), amount, billId,
            "Record disbursement");
        double newPaidAmount = Math.min(bill.getTotalAmount(), bill.getPaidAmount() + amount);
        DocumentStatus newStatus = resolveStatus(bill.getTotalAmount(), newPaidAmount, bill.getDueDate());

        Payment payment = new Payment();
        payment.setPaymentNo("PAY-" + System.currentTimeMillis());
        payment.setPaymentType(PaymentType.DISBURSEMENT);
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setReferenceNo(referenceNo);
        payment.setBillId(billId);
        payment = paymentRepository.create(payment);

        boolean updated = billRepository.updatePayment(billId, newPaidAmount, newStatus);
        if (updated) {
            getProcurementFinanceExpenseSyncService().syncSupplierDisbursement(bill, payment);
            complianceService.logAudit(
                    "payments",
                    payment.getId(),
                    "CREATE",
                    null,
                    toPaymentJson(payment),
                    "Bill disbursement recorded");
        }
        return updated;
    }

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public double getOpenPayableTotal() {
        return billRepository.getOpenPayableTotal();
    }

    private DocumentStatus resolveStatus(double totalAmount, double paidAmount, LocalDate dueDate) {
        if (paidAmount >= totalAmount && totalAmount > 0.0) {
            return DocumentStatus.PAID;
        }
        if (paidAmount > 0.0 && paidAmount < totalAmount) {
            return DocumentStatus.PARTIALLY_PAID;
        }
        if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
            return DocumentStatus.OVERDUE;
        }
        return DocumentStatus.ISSUED;
    }

    private String toBillJson(Bill bill) {
        if (bill == null) {
            return null;
        }
        return "{"
                + "\"id\":\"" + esc(bill.getId()) + "\","
                + "\"bill_no\":\"" + esc(bill.getBillNo()) + "\","
                + "\"vendor_id\":\"" + esc(bill.getVendorId()) + "\","
                + "\"total_amount\":" + bill.getTotalAmount() + ","
                + "\"status\":\"" + (bill.getStatus() == null ? "" : bill.getStatus().name()) + "\""
                + "}";
    }

    private String toPaymentJson(Payment payment) {
        if (payment == null) {
            return null;
        }
        return "{"
                + "\"payment_no\":\"" + esc(payment.getPaymentNo()) + "\","
                + "\"payment_type\":\"" + (payment.getPaymentType() == null ? "" : payment.getPaymentType().name()) + "\","
                + "\"amount\":" + payment.getAmount() + ","
                + "\"bill_id\":\"" + esc(payment.getBillId()) + "\""
                + "}";
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private ProcurementFinanceExpenseSyncService getProcurementFinanceExpenseSyncService() {
        if (procurementFinanceExpenseSyncService == null) {
            procurementFinanceExpenseSyncService = ProcurementFinanceExpenseSyncService.createDefault();
        }
        return procurementFinanceExpenseSyncService;
    }
}
