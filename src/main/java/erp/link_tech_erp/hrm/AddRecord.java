package erp.link_tech_erp.hrm;

import java.util.*;

public class AddRecord {

    public static void add(){
        try (Scanner sc = new Scanner(System.in)) {
            ArrayList<String[]> employees = DatabaseConnection.loadData();

        System.out.println("\n=== Add Employee ===");

        System.out.print("Employee ID: ");
        String id = sc.nextLine();

        System.out.print("Name: ");
        String name = sc.nextLine();

        System.out.print("Position: ");
        String position = sc.nextLine();

        System.out.print("Department: ");
        String department = sc.nextLine();

        System.out.print("Salary: ");
        String salary = sc.nextLine();

            employees.add(new String[]{id,name,position,department,salary});
            DatabaseConnection.saveData(employees);
            System.out.println("Employee Added Successfully");
        }
    }
}
