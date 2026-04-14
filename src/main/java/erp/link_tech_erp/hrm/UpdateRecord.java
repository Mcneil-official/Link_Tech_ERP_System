package erp.link_tech_erp.hrm;

import java.util.*;

public class UpdateRecord {

    public static void update(){
        Scanner sc = new Scanner(System.in);
        try {
            ArrayList<String[]> employees = DatabaseConnection.loadData();

            System.out.print("Enter Employee ID: ");
            String id = sc.nextLine();

        for(String[] emp : employees){

            if(emp[0].equals(id)){

                System.out.print("New Name: ");
                emp[1] = sc.nextLine();

                System.out.print("New Position: ");
                emp[2] = sc.nextLine();

                System.out.print("New Department: ");
                emp[3] = sc.nextLine();

                System.out.print("New Salary: ");
                emp[4] = sc.nextLine();

                DatabaseConnection.saveData(employees);

                System.out.println("Employee Updated");
                return;
            }
        }

        System.out.println("Employee Not Found");
        } finally {
            sc.close();
        }
    }
}
