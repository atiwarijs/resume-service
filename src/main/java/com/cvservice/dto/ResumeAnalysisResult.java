package com.cvservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisResult {
    private String originalFileName;
    private String s3FileUrl;
    private String extractedText;
    private String aiAnalysis;
}
