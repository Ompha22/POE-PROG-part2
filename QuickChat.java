package ChatAppPOE;

import java.util.Scanner;

public class QuickChat {

    public static void main(String[] args) {

        Scanner keyboard = new Scanner(System.in);

        displayHeading();

        System.out.println("CREATE YOUR ACCOUNT");
        System.out.println("-------------------");
        
        //prompt the user to enter their first name
        System.out.print("First name: ");
        String name = keyboard.nextLine().trim();
       
        //prompt the user to enter their last name
        System.out.print("Last name: ");
        String surname = keyboard.nextLine().trim();
        
        //prompt the user to enter their Username
        System.out.print("Username: ");
        String username = keyboard.nextLine().trim();
        
        //prompt the user to enter their password
        System.out.print("Password: ");
        String password = keyboard.nextLine();
        
        //prompt the user to enter their cellphone number (+27)
        System.out.print("Cellphone number (+27): ");
        String cellphone = keyboard.nextLine().trim();


        Login account = new Login(
                name,
                surname,
                username,
                password,
                cellphone
        );


        System.out.println();
        System.out.println("VALIDATION RESULTS");
        System.out.println("------------------");

        System.out.println(account.usernameMessage());
        System.out.println(account.passwordMessage());


        if (account.checkCellPhoneNumber()) {

            System.out.println(
                    "Cell phone number successfully added."
            );

        } else {

            System.out.println(
                    "Cell phone number incorrectly formatted "
                    + "or does not contain international code."
            );
        }


        System.out.println();
        System.out.println("REGISTRATION");
        System.out.println("------------");

        String registrationMessage = account.registerUser();

        System.out.println(registrationMessage);


        boolean registrationValid =
                account.checkUserName()
                && account.checkPasswordComplexity()
                && account.checkCellPhoneNumber();


        if (registrationValid) {

            performLogin(keyboard, account);

        } else {

            System.out.println();
            System.out.println(
                    "Account creation failed. "
                    + "Please enter valid registration details."
            );
        }

        keyboard.close();
    }


    private static void displayHeading() {

        System.out.println(
                "======================================"
        );

        System.out.println(
                "             QUICKCHAT"
        );

        System.out.println(
                "======================================"
        );

        System.out.println();
    }


    private static void performLogin(Scanner keyboard,
                                     Login account) {

        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                "                LOGIN"
        );

        System.out.println(
                "======================================"
        );

        System.out.print("Username: ");
        String enteredName = keyboard.nextLine().trim();

        System.out.print("Password: ");
        String enteredPassword = keyboard.nextLine();

        String loginResult =
                account.returnLoginStatus(
                        enteredName,
                        enteredPassword
                );

        System.out.println();
        System.out.println(loginResult);
    }
}