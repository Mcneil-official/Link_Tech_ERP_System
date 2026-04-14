package erp.link_tech_erp.hrm;

import java.util.*;

public class SearchRecord {
    public static void search(){
        try (Scanner sc = new Scanner(System.in)) {
            ArrayList<String[]> employees = DatabaseConnection.loadData();
            System.out.print("Enter Employee Name: ");
            String name = sc.nextLine();

        for(String[] emp : employees){
            if(emp.length > 1 && emp[1].equalsIgnoreCase(name)){
                System.out.println("\nEmployee Found:");
                System.out.println("ID: " + emp[0]);
                System.out.println("Name: " + emp[1]);
                System.out.println("Position: " + emp[2]);
                System.out.println("Department: " + emp[3]);
                System.out.println("Salary: " + emp[4]);
                return;
            }
        }
            System.out.println("Employee Not Found");
        }
    }
}
