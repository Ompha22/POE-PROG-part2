package ChatAppPOE;

import java.util.Scanner;

public class Registration {

    public static void main(String[] args) {
        Scanner keyboard = new Scanner(System.in);

        displayHeading();
        Login account = registerAccount(keyboard);

        boolean registrationValid = account.checkUserName()
                && account.checkPasswordComplexity()
                && account.checkCellPhoneNumber();

        if (registrationValid) {
            boolean loginSuccessful = performLogin(keyboard, account);
            if (loginSuccessful) {
                System.out.println();
                System.out.println("Welcome to QuickChat.");

                int messageLimit = readNonNegativeInteger(
                        keyboard,
                        "How many messages would you like to enter? "
                );
                runMessageMenu(keyboard, messageLimit);
            }
        } else {
            System.out.println();
            System.out.println("Account creation failed. Please enter valid registration details.");
        }

        keyboard.close();
    }

    private static Login registerAccount(Scanner keyboard) {
        System.out.println("CREATE YOUR ACCOUNT");
        System.out.println("-------------------");

        System.out.print("First name: ");
        String firstName = keyboard.nextLine().trim();

        System.out.print("Last name: ");
        String lastName = keyboard.nextLine().trim();

        System.out.print("Username: ");
        String username = keyboard.nextLine().trim();

        System.out.print("Password: ");
        String password = keyboard.nextLine();

        System.out.print("Cellphone number (+27 and nine digits): ");
        String cellphone = keyboard.nextLine().trim();

        Login account = new Login(firstName, lastName, username, password, cellphone);

        System.out.println();
        System.out.println("VALIDATION RESULTS");
        System.out.println("------------------");
        System.out.println(account.usernameMessage());
        System.out.println(account.passwordMessage());
        System.out.println(account.cellPhoneMessage());

        System.out.println();
        System.out.println("REGISTRATION");
        System.out.println("------------");
        System.out.println(account.registerUser());

        return account;
    }

    private static void displayHeading() {
        System.out.println("======================================");
        System.out.println("              QUICKCHAT");
        System.out.println("======================================");
        System.out.println();
    }

    /** Returns true only when the entered login credentials are accepted. */
    private static boolean performLogin(Scanner keyboard, Login account) {
        System.out.println();
        System.out.println("======================================");
        System.out.println("                 LOGIN");
        System.out.println("======================================");

        System.out.print("Username: ");
        String enteredUsername = keyboard.nextLine().trim();

        System.out.print("Password: ");
        String enteredPassword = keyboard.nextLine();

        String loginResult = account.returnLoginStatus(enteredUsername, enteredPassword);
        System.out.println();
        System.out.println(loginResult);
        return account.loginUser(enteredUsername, enteredPassword);
    }

    private static void runMessageMenu(Scanner keyboard, int messageLimit) {
        int messagesProcessed = 0;
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("========== QUICKCHAT MENU ==========");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.print("Choose an option (1-3): ");

            String option = keyboard.nextLine().trim();
            switch (option) {
                case "1":
                    if (messagesProcessed >= messageLimit) {
                        System.out.println("You have entered the maximum number of messages selected.");
                    } else {
                        messagesProcessed = sendMessages(
                                keyboard, messagesProcessed, messageLimit
                        );
                        if (messagesProcessed >= messageLimit) {
                            System.out.println("All selected message entries have been processed.");
                            System.out.println("Total messages sent successfully: "
                                    + Message.getTotalMessagesSent());
                        }
                    }
                    break;
                case "2":
                    // This feature is explicitly marked as in development in Part 2.
                    System.out.println("Coming Soon.");
                    break;
                case "3":
                    System.out.println("Total messages sent successfully: "
                            + Message.getTotalMessagesSent());
                    System.out.println("Thank you for using QuickChat. Goodbye.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid menu option. Please enter 1, 2, or 3.");
                    break;
            }
        }
    }

    /** Uses the loop counter as the zero-based message number required by the hash example. */
    private static int sendMessages(Scanner keyboard,
                                    int firstMessageNumber,
                                    int messageLimit) {
        int messagesProcessed = firstMessageNumber;

        for (int messageNumber = firstMessageNumber;
             messageNumber < messageLimit;
             messageNumber++) {

            System.out.println();
            System.out.println("MESSAGE " + (messageNumber + 1) + " OF " + messageLimit);

            System.out.print("Recipient cell number (+27 followed by nine digits): ");
            String recipient = keyboard.nextLine().trim();

            Message message;
            while (true) {
                System.out.print("Enter a message (maximum 250 characters): ");
                String messageText = keyboard.nextLine();
                message = new Message(messageNumber, recipient, messageText);
                String lengthResult = message.checkMessageLength();
                if ("Message ready to send.".equals(lengthResult)) {
                    break;
                }
                System.out.println(lengthResult);
            }

            System.out.println(message.checkRecipientCell());
            System.out.println("Message ID generated: " + message.getMessageID());
            System.out.println("Message Hash: " + message.createMessageHash());

            while (!message.isCompleted()) {
                System.out.println("Choose what to do with this message:");
                System.out.println("1) Send Message");
                System.out.println("2) Disregard Message");
                System.out.println("3) Store Message to send later");
                System.out.print("Select 1, 2, or 3: ");

                String action = keyboard.nextLine().trim();
                String result = message.SentMessage(action);
                System.out.println(result);

                if (message.isCompleted() && "SENT".equals(message.getStatus())) {
                    System.out.println("Full message details:");
                    System.out.println(message.getFullDetails());
                }
            }

            messagesProcessed++;
        }

        return messagesProcessed;
    }

    private static int readNonNegativeInteger(Scanner keyboard, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = keyboard.nextLine().trim();
            try {
                int number = Integer.parseInt(input);
                if (number >= 0) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
                // The message below gives the user a clear retry instruction.
            }
            System.out.println("Please enter a whole number that is zero or greater.");
        }
    }
}
