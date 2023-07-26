package com.genesistech.njangiApi.helper;

import lombok.NonNull;

public class PasswordValidator {

    /**
     * Checks if password matches password criteria
     * @param password
     * @return newly created password
     */
    public static boolean isValid(@NonNull String password) {
        if (password.length() < 8) {
            return false;
        }
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasSymbol = false;
        boolean hasNumber = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            } else {
                hasSymbol = true;
            }
        }
        return hasUppercase && hasLowercase && hasNumber && hasSymbol;
    }
}
