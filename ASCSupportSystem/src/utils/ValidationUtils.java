package utils;

import exceptions.ValidationException;

public class ValidationUtils {
    
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }
    
    public static boolean isValidPhone(String phone) {
        String phoneRegex = "^[0-9-+()]{10,15}$";
        return phone != null && phone.matches(phoneRegex);
    }
    
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    public static void validateUserInput(String fullName, String email, String phone, 
                                         String username, String password) throws ValidationException {
        if (!isNotEmpty(fullName)) {
            throw new ValidationException("Full name is required");
        }
        if (!isValidEmail(email)) {
            throw new ValidationException("Invalid email format");
        }
        if (!isValidPhone(phone)) {
            throw new ValidationException("Invalid phone number format");
        }
        if (!isNotEmpty(username)) {
            throw new ValidationException("Username is required");
        }
        if (!isNotEmpty(password) || password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
    }
}