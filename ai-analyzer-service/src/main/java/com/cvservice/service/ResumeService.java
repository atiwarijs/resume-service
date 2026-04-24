package com.cvservice.service;

import com.cvservice.aiclient.AIClient;
import com.cvservice.dto.ResumeAnalysisResult;
import com.cvservice.exception.AIServiceException;
import com.cvservice.exception.ResumeProcessingException;
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

    public ResumeAnalysisResult processResume(MultipartFile file, String jobDescription) {
        try {
            log.debug("Starting resume processing for file: {}", file.getOriginalFilename());
            
            // Extract text from resume file
            log.debug("Extracting text from file");
            String extractedText = extractTextFromFile(file);
            log.debug("Extracted text length: {}", extractedText.length());
            
            // Analyze resume with AI
            log.debug("Analyzing resume with AI");
            String aiAnalysis = aiClient.analyzeResume(extractedText, jobDescription);
            log.debug("AI analysis completed");
            
            // Check if AI analysis succeeded before uploading to S3
            if (aiAnalysis.contains("\"atsScore\": 0") && aiAnalysis.contains("AI service temporarily unavailable")) {
                log.warn("AI analysis failed, skipping S3 upload");
                throw new AIServiceException("AI analysis failed - cannot proceed with resume processing");
            }
            
            // Upload original file to S3 only if AI analysis succeeded
            log.debug("Uploading file to S3");
            String s3Url = s3Service.uploadFile(file);
            log.debug("File uploaded to S3: {}", s3Url);
            
            ResumeAnalysisResult result = new ResumeAnalysisResult(
                file.getOriginalFilename(),
                s3Url,
                extractedText,
                aiAnalysis
            );
            
            log.debug("Resume processing completed successfully");
            return result;
            
        } catch (IOException e) {
            log.error("Error reading resume file: {}", e.getMessage(), e);
            throw new ResumeProcessingException("Failed to read resume file: " + e.getMessage(), e);
        } catch (AIServiceException e) {
            log.error("AI service error during resume processing: {}", e.getMessage(), e);
            throw e; // Re-throw AI service exceptions as-is
        } catch (Exception e) {
            log.error("Unexpected error processing resume: {}", e.getMessage(), e);
            throw new ResumeProcessingException("Failed to process resume: " + e.getMessage(), e);
        }
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        Resource resource = new ByteArrayResource(file.getBytes());
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();
        return documents.isEmpty() ? "" : documents.get(0).getText();
    }
}
