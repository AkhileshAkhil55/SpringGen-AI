package com.springgen.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springgen.ai.dto.DownloadRequest;
import com.springgen.ai.dto.ProjectHistoryResponse;
import com.springgen.ai.dto.ProjectMetadata;
import com.springgen.ai.dto.PromptRequest;
import com.springgen.ai.entity.ProjectHistory;
import com.springgen.ai.exception.ResourceNotFoundException;
import com.springgen.ai.repository.ProjectHistoryRepository;
import com.springgen.ai.service.AIService;
import com.springgen.ai.service.ProjectGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final AIService aiService;
    private final ProjectGeneratorService generatorService;
    private final ProjectHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/analyze")
    public ResponseEntity<ProjectMetadata> analyzeProject(@RequestBody PromptRequest request) {

        System.out.println("Analyze API called");
        System.out.println("Prompt = " + request.getPrompt());

        ProjectMetadata metadata = aiService.analyzePrompt(request.getPrompt());
        return ResponseEntity.ok(metadata);
    }

    @PostMapping("/preview")
    public ResponseEntity<Map<String, String>> previewFiles(@RequestBody ProjectMetadata metadata) {
        Map<String, String> files = generatorService.generateProjectFiles(metadata);
        return ResponseEntity.ok(files);
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadProject(@RequestBody DownloadRequest request) {
        try {
            byte[] zipBytes = generatorService.generateProjectZip(request.getMetadata());

            String jsonMetadata = objectMapper.writeValueAsString(request.getMetadata());

            ProjectHistory history = ProjectHistory.builder()
                    .projectName(request.getMetadata().getProjectName())
                    .prompt(request.getPrompt() != null && !request.getPrompt().trim().isEmpty() 
                            ? request.getPrompt() 
                            : "Custom Configuration")
                    .structuredJson(jsonMetadata)
                    .createdAt(LocalDateTime.now())
                    .zipContent(zipBytes)
                    .build();

            historyRepository.save(history);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + request.getMetadata().getProjectName() + ".zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error processing project download", e);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ProjectHistoryResponse>> getHistory() {
        List<ProjectHistoryResponse> list = historyRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ph -> ProjectHistoryResponse.builder()
                        .id(ph.getId())
                        .projectName(ph.getProjectName())
                        .prompt(ph.getPrompt())
                        .structuredJson(ph.getStructuredJson())
                        .createdAt(ph.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/history/{id}/download")
    public ResponseEntity<byte[]> downloadHistoryZip(@PathVariable Long id) {
        ProjectHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project history not found with ID: " + id));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + history.getProjectName() + ".zip\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(history.getZipContent());
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long id) {
        ProjectHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project history not found with ID: " + id));

        historyRepository.delete(history);
        return ResponseEntity.noContent().build();
    }
}
