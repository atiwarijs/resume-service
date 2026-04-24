package com.cv.security.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cv.security.dto.PasswordResetRequest;
import com.cv.security.dto.PasswordResetResponse;
import com.cv.security.service.PasswordResetService;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    /**
     * Send password reset link to user's email
     * 
     * @param request Password reset request containing email
     * @return PasswordResetResponse with status
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@RequestBody PasswordResetRequest request) {
        logger.debug("Password reset request received for email: {}", request.getEmail());
        
        PasswordResetResponse response = passwordResetService.sendPasswordResetLink(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Reset password using token
     * 
     * @param request Map containing reset token and new password
     * @return PasswordResetResponse with status
     */
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(PasswordResetResponse.failure("Reset token is required"));
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(PasswordResetResponse.failure("New password is required"));
        }
        
        logger.debug("Password reset request received with token: {}", token);
        
        PasswordResetResponse response = passwordResetService.resetPassword(token, newPassword);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
