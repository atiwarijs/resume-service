package com.cv.security.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cv.security.service.PasswordResetService;

@RestController
@RequestMapping("/auth")
public class PasswordResetDebugController {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetDebugController.class);
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    /**
     * Debug endpoint to check if reset token is valid
     * 
     * @param token Reset token to validate
     * @return Validation result
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Object>> validateResetToken(@RequestParam String token) {
        logger.debug("Validating reset token: {}", token);
        
        try {
            // This is a simple check - in production you'd validate against database with expiry
            boolean isValid = token != null && !token.trim().isEmpty() && token.length() == 36;
            
            return ResponseEntity.ok(Map.of(
                "token", token,
                "valid", isValid,
                "message", isValid ? "Token format is valid" : "Invalid token format"
            ));
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Token validation failed",
                "message", e.getMessage()
            ));
        }
    }
}
