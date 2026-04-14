package erp.link_tech_erp.finance.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import erp.link_tech_erp.finance.model.FinancialRecord;
import erp.link_tech_erp.finance.model.RecordType;
import erp.link_tech_erp.finance.repository.FinancialRecordRepository;

public class FinanceService {
    private final FinancialRecordRepository repository;
    private final ComplianceService complianceService;

    public FinanceService(FinancialRecordRepository repository, ComplianceService complianceService) {
        this.repository = repository;
        this.complianceService = complianceService;
    }

    public List<FinancialRecord> getAllRecords() {
        List<FinancialRecord> records = repository.findAll();
        records.sort(Comparator.comparing(FinancialRecord::getDate).reversed());
        return records;
    }

    public FinancialRecord createRecord(FinancialRecord record) {
        complianceService.assertCanMutate("CREATE_FINANCIAL_RECORD", record.getDate(), record.getAmount(), null,
            "Create financial record");
        record.setId(UUID.randomUUID().toString());
        repository.insert(record);
        complianceService.logAudit(
            "financial_records",
            record.getId(),
            "CREATE",
            null,
            toJson(record),
            "Financial record created");
        return record;
    }

    public boolean updateRecord(FinancialRecord updatedRecord) {
        Optional<FinancialRecord> existing = repository.findById(updatedRecord.getId());
        complianceService.assertCanMutate("UPDATE_FINANCIAL_RECORD", updatedRecord.getDate(), updatedRecord.getAmount(),
            updatedRecord.getId(), "Update financial record");
        boolean updated = repository.update(updatedRecord);
        if (updated) {
            complianceService.logAudit(
                "financial_records",
                updatedRecord.getId(),
                "UPDATE",
                existing.map(this::toJson).orElse(null),
                toJson(updatedRecord),
                "Financial record updated");
        }
        return updated;
    }

    public boolean deleteRecordById(String id) {
        Optional<FinancialRecord> existing = repository.findById(id);
        LocalDate effectiveDate = existing.map(FinancialRecord::getDate).orElse(null);
        double amount = existing.map(FinancialRecord::getAmount).orElse(0.0);
        complianceService.assertCanMutate("DELETE_FINANCIAL_RECORD", effectiveDate, amount, id,
            "Delete financial record");
        boolean deleted = repository.deleteById(id);
        if (deleted) {
            complianceService.logAudit(
                "financial_records",
                id,
                "DELETE",
                existing.map(this::toJson).orElse(null),
                null,
                "Financial record deleted");
        }
        return deleted;
    }

    public Optional<FinancialRecord> findById(String id) {
        return repository.findById(id);
    }

    public FinanceReport generateReport() {
        List<FinancialRecord> records = repository.findAll();
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        Map<String, Double> expenseByCategory = new LinkedHashMap<>();

        for (FinancialRecord record : records) {
            if (record.getType() == RecordType.INCOME) {
                totalIncome += record.getAmount();
            } else if (record.getType() == RecordType.EXPENSE) {
                totalExpense += record.getAmount();
                String category = record.getCategory() == null || record.getCategory().isBlank()
                        ? "Uncategorized"
                        : record.getCategory().trim();
                double currentTotal = expenseByCategory.getOrDefault(category, 0.0);
                expenseByCategory.put(category, currentTotal + record.getAmount());
            }
        }

        return new FinanceReport(totalIncome, totalExpense, totalIncome - totalExpense, records.size(), expenseByCategory);
    }

    private String toJson(FinancialRecord record) {
        if (record == null) {
            return null;
        }
        return "{"
                + "\"id\":\"" + esc(record.getId()) + "\"," 
                + "\"description\":\"" + esc(record.getDescription()) + "\"," 
                + "\"category\":\"" + esc(record.getCategory()) + "\"," 
                + "\"amount\":" + record.getAmount() + ","
                + "\"type\":\"" + (record.getType() == null ? "" : record.getType().name()) + "\"," 
                + "\"date\":\"" + (record.getDate() == null ? "" : record.getDate()) + "\""
                + "}";
    }

    private String esc(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
