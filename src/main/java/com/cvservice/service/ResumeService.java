package com.cvservice.service;

import com.cvservice.aiclient.AIClient;
import com.cvservice.dto.ResumeAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final AIClient aiClient;
    private final S3Service s3Service;

    public ResumeAnalysisResult processResume(MultipartFile file) {
        try {
            // Extract text from resume file
            String extractedText = extractTextFromFile(file);
            
            // Analyze resume with AI
            String aiAnalysis = aiClient.analyzeResume(extractedText);
            
            // Upload original file to S3
            String s3Url = s3Service.uploadFile(file);
            
            return new ResumeAnalysisResult(
                file.getOriginalFilename(),
                s3Url,
                extractedText,
                aiAnalysis
            );
            
        } catch (Exception e) {
            log.error("Error processing resume: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process resume", e);
        }
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        Resource resource = new ByteArrayResource(file.getBytes());
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        return documents.isEmpty() ? "" : documents.get(0).getText();
    }
}
