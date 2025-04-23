package HelperClasses;

import java.util.regex.*;

public class PasswordValidator {

    public static boolean isValidPassword(String password) {
        // At least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}


//ermal.millaku@student.uni-pr.edu
//#HustlinSinceday1