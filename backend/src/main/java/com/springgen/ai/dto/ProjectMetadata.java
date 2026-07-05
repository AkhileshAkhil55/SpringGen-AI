package com.springgen.ai.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProjectMetadata {
    private String projectName;
    private String packageName;
    private String database;
    private String javaVersion = "21";
    private String buildTool = "maven";
    private String entity;
    private List<FieldMetadata> fields;
    private boolean crud = true;
    private boolean lombok = true;
    private boolean security = false;
    private boolean swagger = false;

    @Data
    public static class FieldMetadata {
        private String name;
        private String type;
        private List<String> validations;
    }
}
