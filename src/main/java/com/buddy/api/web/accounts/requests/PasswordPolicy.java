package com.buddy.api.web.accounts.requests;

public final class PasswordPolicy {
    public static final String STRONG_PASSWORD_REGEX =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{6,16}$";
    public static final String STRONG_PASSWORD_MESSAGE =
        "New password must contain uppercase, lowercase, number, and special character";

    private PasswordPolicy() {
    }
}
