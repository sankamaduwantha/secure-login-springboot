package com.userManagement.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Password reset submission carrying the raw token from the reset link.
 *
 * <p>The service layer is responsible for SHA-256 hashing {@code token} before lookup and for
 * comparing {@code newPassword} against {@code confirmPassword}; no cross-field constraint is
 * declared here. Carries no entity types.
 */
public class ResetPasswordRequest {

    @NotBlank(message = "Reset link is invalid or incomplete")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    private String newPassword;

    @NotBlank(message = "Please confirm your new password")
    private String confirmPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String token, String newPassword, String confirmPassword) {
        this.token = token;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /** Deliberately omits the token and both passwords so secrets cannot leak into logs. */
    @Override
    public String toString() {
        return "ResetPasswordRequest{}";
    }
}
