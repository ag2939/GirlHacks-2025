package com.example.shiksharemastered;

public class DataValidator {
    public static boolean validateDate(String Date) {
        if (Date.length() != 10) {
            return false;
        }

        int index = 0;
        while (index < Date.length()) {
            if (Date.charAt(index) == '-') {
                if (index != 2 && index != 5) {
                    return false;
                }
            }
            index++;
        }

        int dd = Integer.parseInt(Date.substring(0, 2));
        int mm = Integer.parseInt(Date.substring(3, 5));
        int yyyy = Integer.parseInt(Date.substring(6, 10));

        if ((dd > 0 && dd < 32) && (mm > 0 && mm < 13) && (yyyy > 0 && yyyy < 2023)) {
            if (dd == 31 || dd == 30 || dd == 29) {
                if (dd == 31 && (mm != 1 && mm != 3 && mm != 4 && mm != 7 && mm != 8 && mm != 10 && mm != 12)) {
                    return false;
                }
                if (dd == 30 && (mm != 5 && mm != 6 && mm != 9 && mm != 11)) {
                    return false;
                }
                return mm == 2 && ((yyyy % 4) == 0);
            }
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean validateFirebaseAddress(String address) {
        char[] addressArray = address.toCharArray();
        for (char x: addressArray) {
            if (x == '.' || x == '[' || x == ']' || x == '/' || x == '$' || x == '#') {
                return false;
            }
        }
        return true;
    }

    public static boolean validateName(String name) {
        int index = 0;
        while (index != name.length()) {
            if (!Character.isLetter(name.charAt(index)) && !Character.isWhitespace(name.charAt(index))) {
                return false;
            }
            index++;
        }
        return true;
    }

    public static boolean validatePhoneNumber(String phone_number) {
        if (phone_number.length() != 10) {
            return false;
        }
        int index = 0;
        while (index != phone_number.length()) {
            if (!Character.isDigit(phone_number.charAt(index))) {
                return false;
            }
            index++;
        }
        return true;
    }

    public static boolean validateEmail(String User_email) {
        return User_email.endsWith("gmail.com") || User_email.endsWith("yahoo.com") || User_email.endsWith("ves.ac.in");
    }

    public static boolean validatePassword(String password) {
        int[] track = new int[4];
        if(password.length() < 8) {
            return false;
        }
        for (int i = 0; i < password.length(); i++) {
            if(Character.isSpaceChar(password.charAt(i))) {
                return false;
            }
            if(Character.isUpperCase(password.charAt(i))) {
                track[0]++;
            }
            else if(Character.isLowerCase(password.charAt(i))) {
                track[1]++;
            }
            else if(Character.isDigit(password.charAt(i))) {
                track[2]++;
            }
            else {
                track[3]++;
            }
        }
        for (int j : track) {
            if (j == 0) {
                return false;
            }
        }
        return true;
    }
}