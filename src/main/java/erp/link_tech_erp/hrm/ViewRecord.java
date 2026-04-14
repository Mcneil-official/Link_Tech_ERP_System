package erp.link_tech_erp.hrm;

import java.util.*;

public class ViewRecord {
    public static void view(){
        ArrayList<String[]> employees = DatabaseConnection.loadData();
        if(employees.isEmpty()){
            System.out.println("No employees found.");
            return;
        }
        System.out.println("\n=== Employee List ===");
        for(String[] emp : employees){
            if(emp.length < 5) continue;
            System.out.println("ID: " + emp[0] + " | Name: " + emp[1] + " | Position: " + emp[2] + " | Department: " + emp[3] + " | Salary: " + emp[4]);
        }
    }
}
