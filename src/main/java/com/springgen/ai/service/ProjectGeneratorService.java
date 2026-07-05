package com.springgen.ai.service;

import com.springgen.ai.dto.ProjectMetadata;
import java.util.Map;

public interface ProjectGeneratorService {
    Map<String, String> generateProjectFiles(ProjectMetadata metadata);
    byte[] generateProjectZip(ProjectMetadata metadata);
}
