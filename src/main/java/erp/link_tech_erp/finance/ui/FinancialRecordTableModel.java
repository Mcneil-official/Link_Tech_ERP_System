package erp.link_tech_erp.finance.ui;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import erp.link_tech_erp.finance.model.FinancialRecord;

public class FinancialRecordTableModel extends AbstractTableModel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] COLUMN_NAMES = {
            "Date", "Type", "Category", "Description", "Amount", "Update", "Delete"
    };

    private List<FinancialRecord> records = new ArrayList<>();

    public void setRecords(List<FinancialRecord> records) {
        this.records = new ArrayList<>(records);
        fireTableDataChanged();
    }

    public FinancialRecord getRecordAt(int rowIndex) {
        return records.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FinancialRecord record = records.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> record.getDate() == null ? "" : DATE_FORMATTER.format(record.getDate());
            case 1 -> record.getType() == null ? "" : record.getType().name();
            case 2 -> record.getCategory();
            case 3 -> record.getDescription();
            case 4 -> String.format("%.2f", record.getAmount());
            case 5 -> "Update";
            case 6 -> "Delete";
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 5 || columnIndex == 6;
    }
}
