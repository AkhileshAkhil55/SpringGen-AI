package com.springgen.ai.service;

import com.springgen.ai.dto.ProjectMetadata;

public interface AIService {
    ProjectMetadata analyzePrompt(String prompt);
}
