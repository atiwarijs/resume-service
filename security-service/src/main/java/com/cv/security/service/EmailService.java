package com.cv.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - EduCamp");
            
            String resetLink = frontendUrl + "/auth/reset-password?token=" + resetToken;
            
            String emailBody = "Hello,\n\n" +
                    "You have requested to reset your password for your EduCamp account.\n\n" +
                    "Please click on the following link to reset your password:\n" +
                    resetLink + "\n\n" +
                    "This link will expire in 15 minutes.\n\n" +
                    "If you did not request this password reset, please ignore this email.\n\n" +
                    "Note: The complete URL should be: " + resetLink + "\n\n" +
                    "Best regards,\n" +
                    "EduCamp Team";
            
            message.setText(emailBody);
            
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
