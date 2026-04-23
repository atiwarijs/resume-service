package com.cvservice.controller;

import com.cvservice.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService service;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestParam("file") MultipartFile file, @RequestParam("jobDescription") String jobDescription) {
        try {
            log.debug("Received file upload request: {}", file.getOriginalFilename());
            log.debug("Received job description");
            return ResponseEntity.ok(service.processResume(file, jobDescription));
        } catch (Exception e) {
            log.error("Error in controller: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error processing file: " + e.getMessage());
        }
    }
}
