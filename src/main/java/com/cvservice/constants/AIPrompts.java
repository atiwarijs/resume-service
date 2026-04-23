package com.cvservice.constants;

public final class AIPrompts {
    
    private AIPrompts() {
        // Utility class - prevent instantiation
    }
    
    public static final String RESUME_ANALYSIS_PROMPT = """
            Analyze the following resume against the provided job description.
            Return strictly in JSON format with:
            1. atsScore (out of 100)
            2. matchingSkills
            3. missingSkills
            4. improvementSuggestions
            5. likelyInterviewQuestions

            Resume:
            %s

            Job Description:
            %s
            """;
            
    public static final String RESUME_ANALYSIS_PROMPT_TEMPLATE = """
            Analyze the following resume against the provided job description.
            Return strictly in JSON format with:
            1. atsScore (out of 100)
            2. matchingSkills
            3. missingSkills
            4. improvementSuggestions
            5. likelyInterviewQuestions

            Resume:
            {resumeText}

            Job Description:
            {jobDescription}
            """;
}
