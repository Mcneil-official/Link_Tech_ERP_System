package erp.link_tech_erp.finance.service;

public class ProfitAndLossReport {
    private final double totalRevenue;
    private final double totalExpense;
    private final double netIncome;

    public ProfitAndLossReport(double totalRevenue, double totalExpense) {
        this.totalRevenue = totalRevenue;
        this.totalExpense = totalExpense;
        this.netIncome = totalRevenue - totalExpense;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetIncome() {
        return netIncome;
    }
}
