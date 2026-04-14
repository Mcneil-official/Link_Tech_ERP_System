package erp.link_tech_erp.hrm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;

public class ViewEmployeeGUI {

    public ViewEmployeeGUI(){

        JFrame frame = new JFrame("Employee List");

        String[] column = {"ID","Name","Position","Department","Salary"};
        DefaultTableModel model = new DefaultTableModel(column,0);

        JTable table = new JTable(model);

        ArrayList<String[]> data = DatabaseConnection.loadData();

        for(String[] emp : data){
            model.addRow(emp);
        }

        frame.add(new JScrollPane(table));
        frame.setSize(500,300);
        frame.setVisible(true);
    }
}
