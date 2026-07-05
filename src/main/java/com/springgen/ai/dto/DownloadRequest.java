package com.springgen.ai.dto;

import lombok.Data;

@Data
public class DownloadRequest {
    private String prompt;
    private ProjectMetadata metadata;
}
