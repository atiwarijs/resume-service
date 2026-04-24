package com.cv.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetResponse {
    
    private String message;
    private boolean success;
    
    public static PasswordResetResponse success(String message) {
        return new PasswordResetResponse(message, true);
    }
    
    public static PasswordResetResponse failure(String message) {
        return new PasswordResetResponse(message, false);
    }
}
