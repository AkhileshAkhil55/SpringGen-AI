package com.springgen.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String projectName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String structuredJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Lob
    @Column(columnDefinition = "LONGBLOB", nullable = false)
    private byte[] zipContent;
}
