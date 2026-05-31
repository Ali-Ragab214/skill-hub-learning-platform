package com.example.Skill_Hub_Learning_Platform.application.validators;

import java.util.regex.Pattern;

public class PasswordValidator {

    //this is a custom regex
    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    public static boolean isStrong(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return pattern.matcher(password).matches();
    }

    public static String getPasswordRequirements() {
        return "Password must contain: 8+ characters, uppercase letter, lowercase letter, digit, special character (@$!%*?&)";
    }
}
