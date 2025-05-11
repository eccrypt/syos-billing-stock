package cli;

import core.dao.UserDAO;
import core.models.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class MainCLI {
    Scanner sc = new Scanner(System.in);
    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_sysos_over_the_counter","root","");
    UserDAO userDAO = new UserDAO(connection);

    public MainCLI() throws SQLException {
        System.out.println("1.Register\n User");
        int choice = sc.nextInt();
        sc.nextLine();
        System.out.println("Enter Username");
        String username = sc.nextLine();
        System.out.println("Enter Password");
        String password = sc.nextLine();
        System.out.println("Enter User Role");
        String userRole = sc.nextLine();
        if (choice == 1){
            userDAO.registerUser(username,password,userRole);
            System.out.println("User Registered Successfully");
        }else{

            System.out.println("Enter an option that is displayed on the menu list.");
        }
    }
    public static void main(String[] args) throws SQLException  {
        System.out.println("SYOS CLI Started");
        new MainCLI();
    }
}