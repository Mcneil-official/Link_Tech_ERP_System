package erp.link_tech_erp.finance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import erp.link_tech_erp.finance.model.DocumentStatus;
import erp.link_tech_erp.finance.model.Invoice;
import erp.link_tech_erp.finance.model.Payment;
import erp.link_tech_erp.finance.model.PaymentType;
import erp.link_tech_erp.finance.repository.InvoiceRepository;
import erp.link_tech_erp.finance.repository.PaymentRepository;

public class AccountsReceivableService {
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ComplianceService complianceService;

    public AccountsReceivableService(InvoiceRepository invoiceRepository,
                                     PaymentRepository paymentRepository,
                                     ComplianceService complianceService) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.complianceService = complianceService;
    }

    public Invoice issueInvoice(Invoice invoice) {
        if (invoice.getIssueDate() == null) {
            invoice.setIssueDate(LocalDate.now());
        }
        if (invoice.getDueDate() == null) {
            invoice.setDueDate(invoice.getIssueDate().plusDays(30));
        }
        if (invoice.getStatus() == null || invoice.getStatus() == DocumentStatus.DRAFT) {
            invoice.setStatus(DocumentStatus.ISSUED);
        }

        double subtotal = invoice.getSubtotal();
        if (subtotal <= 0.0 && invoice.getLines() != null) {
            subtotal = invoice.getLines().stream().mapToDouble(line -> line.getLineTotal()).sum();
            invoice.setSubtotal(subtotal);
        }
        invoice.setTotalAmount(invoice.getSubtotal() + invoice.getTaxAmount());
        complianceService.assertCanMutate("ISSUE_INVOICE", invoice.getIssueDate(), invoice.getTotalAmount(),
            invoice.getInvoiceNo(), "Issue invoice");
        Invoice created = invoiceRepository.create(invoice);
        complianceService.logAudit(
            "invoices",
            created.getId(),
            "CREATE",
            null,
            toInvoiceJson(created),
            "Invoice issued");
        return created;
    }

    public boolean recordReceipt(String invoiceId, double amount, String method, String referenceNo) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        Optional<Invoice> optional = invoiceRepository.findById(invoiceId);
        if (optional.isEmpty()) {
            return false;
        }

        Invoice invoice = optional.get();
        complianceService.assertCanMutate("RECORD_RECEIPT", LocalDate.now(), amount, invoiceId,
            "Record receipt");
        double newPaidAmount = Math.min(invoice.getTotalAmount(), invoice.getPaidAmount() + amount);
        DocumentStatus newStatus = resolveStatus(invoice.getTotalAmount(), newPaidAmount, invoice.getDueDate());

        Payment payment = new Payment();
        payment.setPaymentNo("RCPT-" + System.currentTimeMillis());
        payment.setPaymentType(PaymentType.RECEIPT);
        payment.setPaymentDate(LocalDate.now());
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setReferenceNo(referenceNo);
        payment.setInvoiceId(invoiceId);
        payment = paymentRepository.create(payment);
        boolean updated = invoiceRepository.updatePayment(invoiceId, newPaidAmount, newStatus);
        if (updated) {
            complianceService.logAudit(
                    "payments",
                    payment.getId(),
                    "CREATE",
                    null,
                    toPaymentJson(payment),
                    "Invoice receipt recorded");
        }
        return updated;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public double getOpenReceivableTotal() {
        return invoiceRepository.getOpenReceivableTotal();
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

    private String toInvoiceJson(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        return "{"
                + "\"id\":\"" + esc(invoice.getId()) + "\","
                + "\"invoice_no\":\"" + esc(invoice.getInvoiceNo()) + "\","
                + "\"customer_id\":\"" + esc(invoice.getCustomerId()) + "\","
                + "\"total_amount\":" + invoice.getTotalAmount() + ","
                + "\"status\":\"" + (invoice.getStatus() == null ? "" : invoice.getStatus().name()) + "\""
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
                + "\"invoice_id\":\"" + esc(payment.getInvoiceId()) + "\""
                + "}";
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
