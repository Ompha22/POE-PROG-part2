package ChatAppPOE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login {

    private String firstName;
    private String lastName;
    private String userName;
    private String userPassword;
    private String phoneNumber;

    // South African number: +27 followed by 9 digits.
  
    private static final String PHONE_REGEX = "^\\+27[0-9]{9}$";

    public Login(String firstName,
                 String lastName,
                 String userName,
                 String userPassword,
                 String phoneNumber) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.userPassword = userPassword;
        this.phoneNumber = phoneNumber;
    }


    // Checks whether username contains "_" and has 5 or fewer characters.
    public boolean checkUserName() {

        if (userName == null) {
            return false;
        }

        boolean hasUnderscore = userName.indexOf('_') >= 0;
        boolean correctLength = userName.length() <= 5;

        return hasUnderscore && correctLength;
    }


    // Checks all password complexity requirements.
    public boolean checkPasswordComplexity() {

        if (userPassword == null) {
            return false;
        }

        String passwordPattern =
                "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$";

        return userPassword.matches(passwordPattern);
    }


    // Checks that the number uses the South African international format.
    public boolean checkCellPhoneNumber() {

        if (phoneNumber == null) {
            return false;
        }

        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phoneNumber);

        return matcher.matches();
    }


    public String usernameMessage() {

        if (checkUserName()) {
            return "Username successfully captured.";
        }

        return "Username is not correctly formatted; "
                + "please ensure that your username contains "
                + "an underscore and is no more than five "
                + "characters in length.";
    }


    public String passwordMessage() {

        if (checkPasswordComplexity()) {
            return "Password successfully captured.";
        }

        return "Password is not correctly formatted; "
                + "please ensure that the password contains "
                + "at least eight characters, a capital letter, "
                + "a number, and a special character.";
    }


    public String cellPhoneMessage() {

        if (checkCellPhoneNumber()) {
            return "Cell number successfully captured.";
        }

        return "Cell number is incorrectly formatted or does "
                + "not contain an international code; please "
                + "correct the number and try again.";
    }


    public String registerUser() {

        if (!checkUserName()) {
            return usernameMessage();
        }

        if (!checkPasswordComplexity()) {
            return passwordMessage();
        }

        if (!checkCellPhoneNumber()) {
            return cellPhoneMessage();
        }

        return "User successfully registered.";
    }


    public boolean loginUser(String loginName, String loginPassword) {

        if (loginName == null || loginPassword == null) {
            return false;
        }

        boolean usernameMatches = userName.equals(loginName);
        boolean passwordMatches = userPassword.equals(loginPassword);

        return usernameMatches && passwordMatches;
    }


    public String returnLoginStatus(String loginName,
                                    String loginPassword) {

        if (loginUser(loginName, loginPassword)) {

            return "Welcome " + firstName + ", " + lastName
                    + " it is great to see you again.";
        }

        return "Username or password incorrect, please try again.";
    }
}