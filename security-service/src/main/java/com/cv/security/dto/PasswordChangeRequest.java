package com.cv.security.dto;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private String userId;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
