package erp.link_tech_erp.hrm;

import java.util.*;

public class DeleteRecord {
    public static void delete(){
        try (Scanner sc = new Scanner(System.in)) {
            ArrayList<String[]> employees = DatabaseConnection.loadData();
            System.out.print("Enter Employee ID: ");
            String id = sc.nextLine();

            for(int i=0; i<employees.size(); i++){
            if(employees.get(i).length > 0 && employees.get(i)[0].equals(id)){
                employees.remove(i);
                DatabaseConnection.saveData(employees);
                System.out.println("Employee Deleted");
                return;
            }
        }

            System.out.println("Employee Not Found");
        }
    }
}
