package erp.link_tech_erp.finance.service;

import java.util.Map;

public class FinanceReport {
    private final double totalIncome;
    private final double totalExpense;
    private final double netBalance;
    private final int recordCount;
    private final Map<String, Double> expenseByCategory;

    public FinanceReport(double totalIncome, double totalExpense, double netBalance, int recordCount,
                         Map<String, Double> expenseByCategory) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.recordCount = recordCount;
        this.expenseByCategory = expenseByCategory;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetBalance() {
        return netBalance;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public Map<String, Double> getExpenseByCategory() {
        return expenseByCategory;
    }
}
