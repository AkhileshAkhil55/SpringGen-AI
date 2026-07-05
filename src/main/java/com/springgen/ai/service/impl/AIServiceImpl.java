package com.springgen.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springgen.ai.dto.ProjectMetadata;
import com.springgen.ai.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public ProjectMetadata analyzePrompt(String prompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Gemini API key is not configured. Running offline local parser fallback.");
            return runLocalParser(prompt);
        }

        try {
            log.info("Sending prompt to Gemini API for analysis...");
            String systemInstruction = "You are a system that converts natural language requests for Spring Boot apps into a structured JSON configuration.\n" +
                    "Analyze the user prompt and extract the project configurations and entity details.\n" +
                    "Return ONLY a valid JSON object matching the following structure:\n" +
                    "{\n" +
                    "  \"projectName\": \"ProjectName (alphanumeric, no spaces)\",\n" +
                    "  \"packageName\": \"com.example.projectname\",\n" +
                    "  \"database\": \"mysql\" | \"postgresql\" | \"mongodb\" | \"sqlserver\",\n" +
                    "  \"javaVersion\": \"21\",\n" +
                    "  \"buildTool\": \"maven\",\n" +
                    "  \"entity\": \"EntityName (singular, PascalCase)\",\n" +
                    "  \"fields\": [\n" +
                    "    {\n" +
                    "      \"name\": \"fieldName (camelCase)\",\n" +
                    "      \"type\": \"Long\" | \"String\" | \"double\" | \"int\" | \"boolean\" | \"LocalDate\" | \"LocalDateTime\",\n" +
                    "      \"validations\": [\"@Id\", \"@GeneratedValue(strategy = GenerationType.IDENTITY)\", \"@NotNull\", \"@NotBlank\", \"@Email\", \"@Size(min=..., max=...)\"]\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"crud\": true,\n" +
                    "  \"lombok\": true,\n" +
                    "  \"security\": true,\n" +
                    "  \"swagger\": true\n" +
                    "}\n" +
                    "Do not write markdown backticks or other text. Just return the JSON object.";

            String requestBody = objectMapper.writeValueAsString(new GeminiRequest(prompt, systemInstruction));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                String jsonText = root.path("candidates")
                        .get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text")
                        .asText()
                        .trim();
                
                // Clean markdown if returned
                if (jsonText.startsWith("```json")) {
                    jsonText = jsonText.substring(7);
                }
                if (jsonText.endsWith("```")) {
                    jsonText = jsonText.substring(0, jsonText.length() - 3);
                }
                jsonText = jsonText.trim();

                log.info("Gemini AI successfully returned structured JSON.");
                return objectMapper.readValue(jsonText, ProjectMetadata.class);
            } else {
                log.error("Gemini API call failed with status: {}. Body: {}. Falling back to local parser.", 
                        response.statusCode(), response.body());
                return runLocalParser(prompt);
            }

        } catch (Exception e) {
            log.error("Exception during Gemini API call: {}. Falling back to local parser.", e.getMessage(), e);
            return runLocalParser(prompt);
        }
    }

    private ProjectMetadata runLocalParser(String prompt) {
        ProjectMetadata metadata = new ProjectMetadata();
        metadata.setProjectName("SampleProject");
        metadata.setPackageName("com.example.sample");
        metadata.setDatabase("mysql");
        metadata.setJavaVersion("21");
        metadata.setBuildTool("maven");
        metadata.setCrud(true);
        metadata.setLombok(true);
        metadata.setSecurity(false);
        metadata.setSwagger(false);

        // Extract Project Name
        Pattern projectPattern = Pattern.compile("(?i)(?:create|build|generate)\\s+(?:a|an)?\\s*([a-zA-Z0-9_\\s]+?)(?:\\s+system|\\s+application|$)", Pattern.MULTILINE);
        Matcher projectMatcher = projectPattern.matcher(prompt);
        if (projectMatcher.find()) {
            String match = projectMatcher.group(1).trim();
            String[] words = match.split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
                }
            }
            if (sb.length() > 0) {
                metadata.setProjectName(sb.toString());
                metadata.setPackageName("com.example." + sb.toString().toLowerCase());
            }
        }

        // Database
        String lowerPrompt = prompt.toLowerCase();
        if (lowerPrompt.contains("mysql")) {
            metadata.setDatabase("mysql");
        } else if (lowerPrompt.contains("postgres") || lowerPrompt.contains("postgresql")) {
            metadata.setDatabase("postgresql");
        } else if (lowerPrompt.contains("mongo") || lowerPrompt.contains("mongodb")) {
            metadata.setDatabase("mongodb");
        } else if (lowerPrompt.contains("sql server") || lowerPrompt.contains("sqlserver") || lowerPrompt.contains("mssql")) {
            metadata.setDatabase("sqlserver");
        }

        // Flags
        if (lowerPrompt.contains("security")) {
            metadata.setSecurity(true);
        }
        if (lowerPrompt.contains("swagger") || lowerPrompt.contains("openapi")) {
            metadata.setSwagger(true);
        }
        if (lowerPrompt.contains("lombok")) {
            metadata.setLombok(true);
        }

        // Entity
        Pattern entityPattern = Pattern.compile("(?i)entity:\\s*([a-zA-Z0-9_]+)");
        Matcher entityMatcher = entityPattern.matcher(prompt);
        if (entityMatcher.find()) {
            metadata.setEntity(entityMatcher.group(1).trim());
        } else {
            if (metadata.getProjectName().endsWith("Management") && metadata.getProjectName().length() > 10) {
                metadata.setEntity(metadata.getProjectName().substring(0, metadata.getProjectName().length() - 10));
            } else {
                metadata.setEntity("Item");
            }
        }

        // Fields
        List<ProjectMetadata.FieldMetadata> fields = new ArrayList<>();
        String[] lines = prompt.split("\\r?\\n");
        boolean inFieldsSection = false;

        for (String line : lines) {
            line = line.trim();
            if (line.toLowerCase().startsWith("fields:")) {
                inFieldsSection = true;
                continue;
            }
            if (line.toLowerCase().startsWith("database") || line.toLowerCase().startsWith("use") || line.toLowerCase().startsWith("generate")) {
                inFieldsSection = false;
            }

            if (inFieldsSection && !line.isEmpty()) {
                Pattern fieldPattern = Pattern.compile("^([a-zA-Z0-9_]+)\\s+([a-zA-Z0-9_]+)");
                Matcher fieldMatcher = fieldPattern.matcher(line);
                if (fieldMatcher.find()) {
                    String fieldName = fieldMatcher.group(1);
                    String fieldType = fieldMatcher.group(2);

                    if (isKnownType(fieldType)) {
                        ProjectMetadata.FieldMetadata field = new ProjectMetadata.FieldMetadata();
                        field.setName(fieldName);
                        field.setType(normalizeType(fieldType));

                        List<String> validations = new ArrayList<>();
                        if (fieldName.equalsIgnoreCase("id")) {
                            validations.add("@Id");
                            validations.add("@GeneratedValue(strategy = GenerationType.IDENTITY)");
                        } else {
                            if (field.getType().equals("String")) {
                                if (fieldName.toLowerCase().contains("email")) {
                                    validations.add("@Email");
                                    validations.add("@NotBlank");
                                } else {
                                    validations.add("@NotBlank");
                                    validations.add("@Size(min = 2, max = 100)");
                                }
                            } else {
                                validations.add("@NotNull");
                            }
                        }
                        field.setValidations(validations);
                        fields.add(field);
                    }
                }
            }
        }

        // Fallback fields if nothing matches
        if (fields.isEmpty()) {
            ProjectMetadata.FieldMetadata idField = new ProjectMetadata.FieldMetadata();
            idField.setName("id");
            idField.setType("Long");
            idField.setValidations(List.of("@Id", "@GeneratedValue(strategy = GenerationType.IDENTITY)"));
            fields.add(idField);

            ProjectMetadata.FieldMetadata nameField = new ProjectMetadata.FieldMetadata();
            nameField.setName("name");
            nameField.setType("String");
            nameField.setValidations(List.of("@NotBlank", "@Size(min = 2, max = 100)"));
            fields.add(nameField);
        }

        metadata.setFields(fields);
        return metadata;
    }

    private boolean isKnownType(String type) {
        String t = type.toLowerCase();
        return t.equals("string") || t.equals("long") || t.equals("double") || t.equals("int") || 
               t.equals("integer") || t.equals("boolean") || t.equals("date") || t.equals("localdate");
    }

    private String normalizeType(String type) {
        String t = type.toLowerCase();
        if (t.equals("string")) return "String";
        if (t.equals("long")) return "Long";
        if (t.equals("double")) return "double";
        if (t.equals("int") || t.equals("integer")) return "int";
        if (t.equals("boolean")) return "boolean";
        if (t.equals("localdate") || t.equals("date")) return "LocalDate";
        return type;
    }

    // Helper classes for Gemini API serialization
    private static class GeminiRequest {
        public final List<Content> contents;
        public final GenerationConfig generationConfig;

        public GeminiRequest(String prompt, String systemInstruction) {
            this.contents = List.of(new Content(List.of(new Part(systemInstruction + "\n\nUser prompt: " + prompt))));
            this.generationConfig = new GenerationConfig("application/json");
        }
    }

    private static class Content {
        public final List<Part> parts;
        public Content(List<Part> parts) { this.parts = parts; }
    }

    private static class Part {
        public final String text;
        public Part(String text) { this.text = text; }
    }

    private static class GenerationConfig {
        public final String responseMimeType;
        public GenerationConfig(String responseMimeType) { this.responseMimeType = responseMimeType; }
    }
}
