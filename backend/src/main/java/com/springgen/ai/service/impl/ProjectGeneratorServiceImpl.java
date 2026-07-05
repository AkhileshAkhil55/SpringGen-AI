package com.springgen.ai.service.impl;

import com.springgen.ai.dto.ProjectMetadata;
import com.springgen.ai.service.ProjectGeneratorService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProjectGeneratorServiceImpl implements ProjectGeneratorService {

    @Override
    public Map<String, String> generateProjectFiles(ProjectMetadata metadata) {
        Map<String, String> files = new HashMap<>();

        String packagePath = "src/main/java/" + metadata.getPackageName().replace('.', '/') + "/";

        // 1. pom.xml
        files.put("pom.xml", generatePomXml(metadata));

        // 2. application.properties
        files.put("src/main/resources/application.properties", generateApplicationProperties(metadata));

        // 3. Main Application Class
        String mainClassName = metadata.getProjectName() + "Application";
        files.put(packagePath + mainClassName + ".java", generateMainApplicationClass(metadata, mainClassName));

        // 4. Entity
        files.put(packagePath + "entity/" + metadata.getEntity() + ".java", generateEntityClass(metadata));

        // 5. Repository
        files.put(packagePath + "repository/" + metadata.getEntity() + "Repository.java", generateRepositoryClass(metadata));

        // 6. Service Interface
        files.put(packagePath + "service/" + metadata.getEntity() + "Service.java", generateServiceInterface(metadata));

        // 7. Service Implementation
        files.put(packagePath + "service/impl/" + metadata.getEntity() + "ServiceImpl.java", generateServiceImplClass(metadata));

        // 8. DTO Request
        files.put(packagePath + "dto/" + metadata.getEntity() + "Request.java", generateRequestDtoClass(metadata));

        // 9. DTO Response
        files.put(packagePath + "dto/" + metadata.getEntity() + "Response.java", generateResponseDtoClass(metadata));

        // 10. ResourceNotFoundException
        files.put(packagePath + "exception/ResourceNotFoundException.java", generateResourceNotFoundException(metadata));

        // 11. GlobalExceptionHandler
        files.put(packagePath + "exception/GlobalExceptionHandler.java", generateGlobalExceptionHandler(metadata));

        // 12. SecurityConfig (conditional)
        if (metadata.isSecurity()) {
            files.put(packagePath + "config/SecurityConfig.java", generateSecurityConfig(metadata));
        }

        return files;
    }

    @Override
    public byte[] generateProjectZip(ProjectMetadata metadata) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            Map<String, String> files = generateProjectFiles(metadata);
            for (Map.Entry<String, String> entry : files.entrySet()) {
                // Prepend project directory name inside the ZIP
                String zipPath = metadata.getProjectName() + "/" + entry.getKey();
                ZipEntry ze = new ZipEntry(zipPath);
                zos.putNextEntry(ze);
                zos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate ZIP archive for project: " + metadata.getProjectName(), e);
        }
        return baos.toByteArray();
    }

    private ProjectMetadata.FieldMetadata getIdField(ProjectMetadata metadata) {
        if (metadata.getFields() != null) {
            for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                if (field.getValidations() != null && field.getValidations().contains("@Id")) {
                    return field;
                }
            }
            for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                if ("id".equalsIgnoreCase(field.getName())) {
                    return field;
                }
            }
        }
        ProjectMetadata.FieldMetadata fallback = new ProjectMetadata.FieldMetadata();
        fallback.setName("id");
        fallback.setType("Long");
        fallback.setValidations(List.of("@Id", "@GeneratedValue(strategy = GenerationType.IDENTITY)"));
        return fallback;
    }

    private String generatePomXml(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
          .append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n")
          .append("         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">\n")
          .append("    <modelVersion>4.0.0</modelVersion>\n")
          .append("    <parent>\n")
          .append("        <groupId>org.springframework.boot</groupId>\n")
          .append("        <artifactId>spring-boot-starter-parent</artifactId>\n")
          .append("        <version>3.4.1</version>\n")
          .append("        <relativePath/> <!-- lookup parent from repository -->\n")
          .append("    </parent>\n")
          .append("    <groupId>").append(metadata.getPackageName()).append("</groupId>\n")
          .append("    <artifactId>").append(metadata.getProjectName().toLowerCase()).append("</artifactId>\n")
          .append("    <version>0.0.1-SNAPSHOT</version>\n")
          .append("    <name>").append(metadata.getProjectName()).append("</name>\n")
          .append("    <description>Generated Spring Boot application</description>\n\n")
          .append("    <properties>\n")
          .append("        <java.version>").append(metadata.getJavaVersion()).append("</java.version>\n")
          .append("        <springdoc.version>2.8.0</springdoc.version>\n")
          .append("    </properties>\n\n")
          .append("    <dependencies>\n")
          .append("        <dependency>\n")
          .append("            <groupId>org.springframework.boot</groupId>\n")
          .append("            <artifactId>spring-boot-starter-web</artifactId>\n")
          .append("        </dependency>\n")
          .append("        <dependency>\n")
          .append("            <groupId>org.springframework.boot</groupId>\n")
          .append("            <artifactId>spring-boot-starter-validation</artifactId>\n")
          .append("        </dependency>\n");

        if ("mongodb".equalsIgnoreCase(metadata.getDatabase())) {
            sb.append("        <dependency>\n")
              .append("            <groupId>org.springframework.boot</groupId>\n")
              .append("            <artifactId>spring-boot-starter-data-mongodb</artifactId>\n")
              .append("        </dependency>\n");
        } else {
            sb.append("        <dependency>\n")
              .append("            <groupId>org.springframework.boot</groupId>\n")
              .append("            <artifactId>spring-boot-starter-data-jpa</artifactId>\n")
              .append("        </dependency>\n");

            if ("postgresql".equalsIgnoreCase(metadata.getDatabase())) {
                sb.append("        <dependency>\n")
                  .append("            <groupId>org.postgresql</groupId>\n")
                  .append("            <artifactId>postgresql</artifactId>\n")
                  .append("            <scope>runtime</scope>\n")
                  .append("        </dependency>\n");
            } else if ("sqlserver".equalsIgnoreCase(metadata.getDatabase())) {
                sb.append("        <dependency>\n")
                  .append("            <groupId>com.microsoft.sqlserver</groupId>\n")
                  .append("            <artifactId>mssql-jdbc</artifactId>\n")
                  .append("            <scope>runtime</scope>\n")
                  .append("        </dependency>\n");
            } else { // default mysql
                sb.append("        <dependency>\n")
                  .append("            <groupId>com.mysql</groupId>\n")
                  .append("            <artifactId>mysql-connector-j</artifactId>\n")
                  .append("            <scope>runtime</scope>\n")
                  .append("        </dependency>\n");
            }
        }

        if (metadata.isSecurity()) {
            sb.append("        <dependency>\n")
              .append("            <groupId>org.springframework.boot</groupId>\n")
              .append("            <artifactId>spring-boot-starter-security</artifactId>\n")
              .append("        </dependency>\n");
        }

        if (metadata.isSwagger()) {
            sb.append("        <dependency>\n")
              .append("            <groupId>org.springdoc</groupId>\n")
              .append("            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>\n")
              .append("            <version>${springdoc.version}</version>\n")
              .append("        </dependency>\n");
        }

        if (metadata.isLombok()) {
            sb.append("        <dependency>\n")
              .append("            <groupId>org.projectlombok</groupId>\n")
              .append("            <artifactId>lombok</artifactId>\n")
              .append("            <optional>true</optional>\n")
              .append("        </dependency>\n");
        }

        sb.append("        <dependency>\n")
          .append("            <groupId>org.springframework.boot</groupId>\n")
          .append("            <artifactId>spring-boot-starter-test</artifactId>\n")
          .append("            <scope>test</scope>\n")
          .append("        </dependency>\n")
          .append("    </dependencies>\n\n")
          .append("    <build>\n")
          .append("        <plugins>\n")
          .append("            <plugin>\n")
          .append("                <groupId>org.springframework.boot</groupId>\n")
          .append("                <artifactId>spring-boot-maven-plugin</artifactId>\n");
        if (metadata.isLombok()) {
            sb.append("                <configuration>\n")
              .append("                    <excludes>\n")
              .append("                        <exclude>\n")
              .append("                            <groupId>org.projectlombok</groupId>\n")
              .append("                            <artifactId>lombok</artifactId>\n")
              .append("                        </exclude>\n")
              .append("                    </excludes>\n")
              .append("                </configuration>\n");
        }
        sb.append("            </plugin>\n")
          .append("        </plugins>\n")
          .append("    </build>\n")
          .append("</project>\n");

        return sb.toString();
    }

    private String generateApplicationProperties(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("server.port=8081\n")
          .append("spring.application.name=").append(metadata.getProjectName()).append("\n\n");

        String dbName = metadata.getProjectName().toLowerCase();

        if ("mongodb".equalsIgnoreCase(metadata.getDatabase())) {
            sb.append("# MongoDB Configurations\n")
              .append("spring.data.mongodb.uri=mongodb://localhost:27017/").append(dbName).append("\n");
        } else {
            sb.append("# Database Configurations\n");
            if ("postgresql".equalsIgnoreCase(metadata.getDatabase())) {
                sb.append("spring.datasource.url=jdbc:postgresql://localhost:5432/").append(dbName).append("?createDatabaseIfNotExist=true\n")
                  .append("spring.datasource.username=postgres\n")
                  .append("spring.datasource.password=postgres\n")
                  .append("spring.datasource.driver-class-name=org.postgresql.Driver\n")
                  .append("spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect\n");
            } else if ("sqlserver".equalsIgnoreCase(metadata.getDatabase())) {
                sb.append("spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=").append(dbName).append(";encrypt=true;trustServerCertificate=true\n")
                  .append("spring.datasource.username=sa\n")
                  .append("spring.datasource.password=Your_Password123\n")
                  .append("spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver\n")
                  .append("spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.SQLServerDialect\n");
            } else { // default mysql
                sb.append("spring.datasource.url=jdbc:mysql://localhost:3306/").append(dbName).append("?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true\n")
                  .append("spring.datasource.username=root\n")
                  .append("spring.datasource.password=\n")
                  .append("spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver\n")
                  .append("spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect\n");
            }
            sb.append("\n# JPA & Hibernate Configurations\n")
              .append("spring.jpa.hibernate.ddl-auto=update\n")
              .append("spring.jpa.show-sql=true\n");
        }

        if (metadata.isSwagger()) {
            sb.append("\n# Swagger API Documentation Configurations\n")
              .append("springdoc.api-docs.path=/v3/api-docs\n")
              .append("springdoc.swagger-ui.path=/swagger-ui.html\n");
        }

        return sb.toString();
    }

    private String generateMainApplicationClass(ProjectMetadata metadata, String className) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(metadata.getPackageName()).append(";\n\n")
          .append("import org.springframework.boot.SpringApplication;\n")
          .append("import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n")
          .append("@SpringBootApplication\n")
          .append("public class ").append(className).append(" {\n")
          .append("    public static void main(String[] args) {\n")
          .append("        SpringApplication.run(").append(className).append(".class, args);\n")
          .append("    }\n")
          .append("}\n");
        return sb.toString();
    }

    private String generateEntityClass(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(metadata.getPackageName()).append(".entity;\n\n");

        boolean isMongo = "mongodb".equalsIgnoreCase(metadata.getDatabase());
        boolean hasDate = false;
        boolean hasDateTime = false;

        if (metadata.getFields() != null) {
            for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                if ("LocalDate".equals(field.getType())) {
                    hasDate = true;
                } else if ("LocalDateTime".equals(field.getType())) {
                    hasDateTime = true;
                }
            }
        }

        if (isMongo) {
            sb.append("import org.springframework.data.annotation.Id;\n")
              .append("import org.springframework.data.mongodb.core.mapping.Document;\n");
        } else {
            sb.append("import jakarta.persistence.*;\n");
        }

        sb.append("import jakarta.validation.constraints.*;\n");

        if (hasDate) {
            sb.append("import java.time.LocalDate;\n");
        }
        if (hasDateTime) {
            sb.append("import java.time.LocalDateTime;\n");
        }

        if (metadata.isLombok()) {
            sb.append("import lombok.AllArgsConstructor;\n")
              .append("import lombok.Builder;\n")
              .append("import lombok.Data;\n")
              .append("import lombok.NoArgsConstructor;\n\n")
              .append("@Data\n")
              .append("@NoArgsConstructor\n")
              .append("@AllArgsConstructor\n")
              .append("@Builder\n");
        } else {
            sb.append("\n");
        }

        if (isMongo) {
            sb.append("@Document(collection = \"").append(metadata.getEntity().toLowerCase()).append("s\")\n");
        } else {
            sb.append("@Entity\n")
              .append("@Table(name = \"").append(metadata.getEntity().toLowerCase()).append("s\")\n");
        }

        sb.append("public class ").append(metadata.getEntity()).append(" {\n\n");

        if (metadata.getFields() != null) {
            for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                if (field.getValidations() != null) {
                    for (String validation : field.getValidations()) {
                        // For MongoDB we don't use @GeneratedValue since it auto-generates String ids or we handle it.
                        // Skip JPA specific annotations if mongo
                        if (isMongo && (validation.contains("GeneratedValue") || validation.contains("GenerationType"))) {
                            continue;
                        }
                        sb.append("    ").append(validation).append("\n");
                    }
                }
                sb.append("    private ").append(field.getType()).append(" ").append(field.getName()).append(";\n\n");
            }
        }

        if (!metadata.isLombok()) {
            sb.append(generateNoArgConstructor(metadata.getEntity()));
            if (metadata.getFields() != null) {
                sb.append(generateAllArgConstructor(metadata.getEntity(), metadata.getFields()));
                sb.append(generateGettersSetters(metadata.getFields()));
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String generateRepositoryClass(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(metadata.getPackageName()).append(".repository;\n\n")
          .append("import ").append(metadata.getPackageName()).append(".entity.").append(metadata.getEntity()).append(";\n");

        boolean isMongo = "mongodb".equalsIgnoreCase(metadata.getDatabase());
        ProjectMetadata.FieldMetadata idField = getIdField(metadata);

        if (isMongo) {
            sb.append("import org.springframework.data.mongodb.repository.MongoRepository;\n");
        } else {
            sb.append("import org.springframework.data.jpa.repository.JpaRepository;\n");
        }

        sb.append("import org.springframework.stereotype.Repository;\n\n")
          .append("@Repository\n")
          .append("public interface ").append(metadata.getEntity()).append("Repository extends ");

        if (isMongo) {
            sb.append("MongoRepository<").append(metadata.getEntity()).append(", ").append(idField.getType()).append("> {\n");
        } else {
            sb.append("JpaRepository<").append(metadata.getEntity()).append(", ").append(idField.getType()).append("> {\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String generateServiceInterface(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        ProjectMetadata.FieldMetadata idField = getIdField(metadata);

        sb.append("package ").append(metadata.getPackageName()).append(".service;\n\n")
          .append("import ").append(metadata.getPackageName()).append(".entity.").append(metadata.getEntity()).append(";\n")
          .append("import java.util.List;\n\n")
          .append("public interface ").append(metadata.getEntity()).append("Service {\n")
          .append("    List<").append(metadata.getEntity()).append("> getAll();\n")
          .append("    ").append(metadata.getEntity()).append(" getById(").append(idField.getType()).append(" id);\n")
          .append("    ").append(metadata.getEntity()).append(" create(").append(metadata.getEntity()).append(" item);\n")
          .append("    ").append(metadata.getEntity()).append(" update(").append(idField.getType()).append(" id, ").append(metadata.getEntity()).append(" item);\n")
          .append("    void delete(").append(idField.getType()).append(" id);\n")
          .append("}\n");
        return sb.toString();
    }

    private String generateServiceImplClass(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        ProjectMetadata.FieldMetadata idField = getIdField(metadata);
        String entityName = metadata.getEntity();
        String serviceName = entityName + "Service";
        String repoName = entityName + "Repository";
        String repoVar = entityName.substring(0, 1).toLowerCase() + entityName.substring(1) + "Repository";

        sb.append("package ").append(metadata.getPackageName()).append(".service.impl;\n\n")
          .append("import ").append(metadata.getPackageName()).append(".entity.").append(entityName).append(";\n")
          .append("import ").append(metadata.getPackageName()).append(".exception.ResourceNotFoundException;\n")
          .append("import ").append(metadata.getPackageName()).append(".repository.").append(repoName).append(";\n")
          .append("import ").append(metadata.getPackageName()).append(".service.").append(serviceName).append(";\n");

        if (metadata.isLombok()) {
            sb.append("import lombok.RequiredArgsConstructor;\n");
        }
        sb.append("import org.springframework.stereotype.Service;\n\n")
          .append("import java.util.List;\n\n")
          .append("@Service\n");

        if (metadata.isLombok()) {
            sb.append("@RequiredArgsConstructor\n")
              .append("public class ").append(entityName).append("ServiceImpl implements ").append(serviceName).append(" {\n\n")
              .append("    private final ").append(repoName).append(" ").append(repoVar).append(";\n\n");
        } else {
            sb.append("public class ").append(entityName).append("ServiceImpl implements ").append(serviceName).append(" {\n\n")
              .append("    private final ").append(repoName).append(" ").append(repoVar).append(";\n\n")
              .append("    public ").append(entityName).append("ServiceImpl(").append(repoName).append(" ").append(repoVar).append(") {\n")
              .append("        this.").append(repoVar).append(" = ").append(repoVar).append(";\n")
              .append("    }\n\n");
        }

        // getAll
        sb.append("    @Override\n")
          .append("    public List<").append(entityName).append("> getAll() {\n")
          .append("        return ").append(repoVar).append(".findAll();\n")
          .append("    }\n\n");

        // getById
        sb.append("    @Override\n")
          .append("    public ").append(entityName).append(" getById(").append(idField.getType()).append(" id) {\n")
          .append("        return ").append(repoVar).append(".findById(id)\n")
          .append("                .orElseThrow(() -> new ResourceNotFoundException(\"").append(entityName).append(" not found with id: \" + id));\n")
          .append("    }\n\n");

        // create
        sb.append("    @Override\n")
          .append("    public ").append(entityName).append(" create(").append(entityName).append(" item) {\n")
          .append("        return ").append(repoVar).append(".save(item);\n")
          .append("    }\n\n");

        // update
        sb.append("    @Override\n")
          .append("    public ").append(entityName).append(" update(").append(idField.getType()).append(" id, ").append(entityName).append(" item) {\n")
          .append("        ").append(entityName).append(" existing = getById(id);\n");

        // Copy properties (except ID)
        if (metadata.getFields() != null) {
            for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                if (field == idField || "id".equalsIgnoreCase(field.getName())) {
                    continue;
                }
                String fieldName = field.getName();
                String capName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                sb.append("        existing.set").append(capName).append("(item.get").append(capName).append("());\n");
            }
        }

        sb.append("        return ").append(repoVar).append(".save(existing);\n")
          .append("    }\n\n");

        // delete
        sb.append("    @Override\n")
          .append("    public void delete(").append(idField.getType()).append(" id) {\n")
          .append("        ").append(entityName).append(" existing = getById(id);\n")
          .append("        ").append(repoVar).append(".delete(existing);\n")
          .append("    }\n")
          .append("}\n");

        return sb.toString();
    }

    private String generateRequestDtoClass(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        String entityName = metadata.getEntity();

        sb.append("package ").append(metadata.getPackageName()).append(".dto;\n\n")
          .append("import ").append(metadata.getPackageName()).append(".entity.").append(entityName).append(";\n")
          .append("import jakarta.validation.constraints.*;\n");

        boolean hasDate = false;
        boolean hasDateTime = false;
        ProjectMetadata.FieldMetadata idField = getIdField(metadata);

        if (metadata.getFields() != null) {
            for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                if ("LocalDate".equals(field.getType())) {
                    hasDate = true;
                } else if ("LocalDateTime".equals(field.getType())) {
                    hasDateTime = true;
                }
            }
        }

        if (hasDate) {
            sb.append("import java.time.LocalDate;\n");
        }
        if (hasDateTime) {
            sb.append("import java.time.LocalDateTime;\n");
        }

        if (metadata.isLombok()) {
            sb.append("import lombok.AllArgsConstructor;\n")
              .append("import lombok.Builder;\n")
              .append("import lombok.Data;\n")
              .append("import lombok.NoArgsConstructor;\n\n")
              .append("@Data\n")
              .append("@NoArgsConstructor\n")
              .append("@AllArgsConstructor\n")
              .append("@Builder\n");
        } else {
            sb.append("\n");
        }

        sb.append("public class ").append(entityName).append("Request {\n\n");

        // Add fields except ID
        if (metadata.getFields() != null) {
            for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                if (field == idField || "id".equalsIgnoreCase(field.getName())) {
                    continue;
                }
                if (field.getValidations() != null) {
                    for (String validation : field.getValidations()) {
                        // Skip JPA mapping annotations
                        if (validation.contains("Id") || validation.contains("GeneratedValue")) {
                            continue;
                        }
                        sb.append("    ").append(validation).append("\n");
                    }
                }
                sb.append("    private ").append(field.getType()).append(" ").append(field.getName()).append(";\n\n");
            }
        }

        // toEntity mapping method
        sb.append("    public ").append(entityName).append(" toEntity() {\n")
          .append("        ").append(entityName).append(" entity = new ").append(entityName).append("();\n");

        if (metadata.getFields() != null) {
            for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                if (field == idField || "id".equalsIgnoreCase(field.getName())) {
                    continue;
                }
                String fieldName = field.getName();
                String capName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                sb.append("        entity.set").append(capName).append("(this.").append(fieldName).append(");\n");
            }
        }
        sb.append("        return entity;\n")
          .append("    }\n\n");

        if (!metadata.isLombok()) {
            sb.append(generateNoArgConstructor(entityName + "Request"));
            // Filter out ID field for constructors/getters/setters
            java.util.ArrayList<ProjectMetadata.FieldMetadata> requestFields = new java.util.ArrayList<>();
            if (metadata.getFields() != null) {
                for (ProjectMetadata.FieldMetadata f : metadata.getFields()) {
                    if (f != idField && !"id".equalsIgnoreCase(f.getName())) {
                        requestFields.add(f);
                    }
                }
            }
            sb.append(generateAllArgConstructor(entityName + "Request", requestFields));
            sb.append(generateGettersSetters(requestFields));
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String generateResponseDtoClass(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        String entityName = metadata.getEntity();
        ProjectMetadata.FieldMetadata idField = getIdField(metadata);

        sb.append("package ").append(metadata.getPackageName()).append(".dto;\n\n")
          .append("import ").append(metadata.getPackageName()).append(".entity.").append(entityName).append(";\n");

        boolean hasDate = false;
        boolean hasDateTime = false;

        if (metadata.getFields() != null) {
            for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                if ("LocalDate".equals(field.getType())) {
                    hasDate = true;
                } else if ("LocalDateTime".equals(field.getType())) {
                    hasDateTime = true;
                }
            }
        }

        if (hasDate) {
            sb.append("import java.time.LocalDate;\n");
        }
        if (hasDateTime) {
            sb.append("import java.time.LocalDateTime;\n");
        }

        if (metadata.isLombok()) {
            sb.append("import lombok.AllArgsConstructor;\n")
              .append("import lombok.Builder;\n")
              .append("import lombok.Data;\n")
              .append("import lombok.NoArgsConstructor;\n\n")
              .append("@Data\n")
              .append("@NoArgsConstructor\n")
              .append("@AllArgsConstructor\n")
              .append("@Builder\n");
        } else {
            sb.append("\n");
        }

        sb.append("public class ").append(entityName).append("Response {\n\n");

        // Add all fields including ID
        if (metadata.getFields() != null) {
            for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                sb.append("    private ").append(field.getType()).append(" ").append(field.getName()).append(";\n\n");
            }
        }

        // static mapping method fromEntity
        sb.append("    public static ").append(entityName).append("Response fromEntity(").append(entityName).append(" entity) {\n")
          .append("        if (entity == null) return null;\n");

        if (metadata.isLombok()) {
            sb.append("        return ").append(entityName).append("Response.builder()\n");
            if (metadata.getFields() != null) {
                for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                    String fieldName = field.getName();
                    String capName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                    sb.append("                .").append(fieldName).append("(entity.get").append(capName).append("())\n");
                }
            }
            sb.append("                .build();\n");
        } else {
            sb.append("        ").append(entityName).append("Response response = new ").append(entityName).append("Response();\n");
            if (metadata.getFields() != null) {
                for (ProjectMetadata.FieldMetadata field : metadata.getFields()) {
                    String fieldName = field.getName();
                    String capName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                    sb.append("        response.set").append(capName).append("(entity.get").append(capName).append("());\n");
                }
            }
            sb.append("        return response;\n");
        }
        sb.append("    }\n\n");

        if (!metadata.isLombok()) {
            sb.append(generateNoArgConstructor(entityName + "Response"));
            if (metadata.getFields() != null) {
                sb.append(generateAllArgConstructor(entityName + "Response", metadata.getFields()));
                sb.append(generateGettersSetters(metadata.getFields()));
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String generateResourceNotFoundException(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(metadata.getPackageName()).append(".exception;\n\n")
          .append("import org.springframework.http.HttpStatus;\n")
          .append("import org.springframework.web.bind.annotation.ResponseStatus;\n\n")
          .append("@ResponseStatus(HttpStatus.NOT_FOUND)\n")
          .append("public class ResourceNotFoundException extends RuntimeException {\n")
          .append("    public ResourceNotFoundException(String message) {\n")
          .append("        super(message);\n")
          .append("    }\n")
          .append("}\n");
        return sb.toString();
    }

    private String generateGlobalExceptionHandler(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(metadata.getPackageName()).append(".exception;\n\n")
          .append("import org.springframework.http.HttpStatus;\n")
          .append("import org.springframework.http.ResponseEntity;\n")
          .append("import org.springframework.validation.FieldError;\n")
          .append("import org.springframework.web.bind.MethodArgumentNotValidException;\n")
          .append("import org.springframework.web.bind.annotation.ExceptionHandler;\n")
          .append("import org.springframework.web.bind.annotation.RestControllerAdvice;\n\n")
          .append("import java.time.LocalDateTime;\n")
          .append("import java.util.HashMap;\n")
          .append("import java.util.Map;\n\n")
          .append("@RestControllerAdvice\n")
          .append("public class GlobalExceptionHandler {\n\n")
          .append("    @ExceptionHandler(ResourceNotFoundException.class)\n")
          .append("    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {\n")
          .append("        Map<String, Object> error = new HashMap<>();\n")
          .append("        error.put(\"timestamp\", LocalDateTime.now());\n")
          .append("        error.put(\"status\", HttpStatus.NOT_FOUND.value());\n")
          .append("        error.put(\"error\", \"Not Found\");\n")
          .append("        error.put(\"message\", ex.getMessage());\n")
          .append("        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);\n")
          .append("    }\n\n")
          .append("    @ExceptionHandler(MethodArgumentNotValidException.class)\n")
          .append("    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {\n")
          .append("        Map<String, Object> error = new HashMap<>();\n")
          .append("        error.put(\"timestamp\", LocalDateTime.now());\n")
          .append("        error.put(\"status\", HttpStatus.BAD_REQUEST.value());\n")
          .append("        error.put(\"error\", \"Bad Request\");\n\n")
          .append("        Map<String, String> validationErrors = new HashMap<>();\n")
          .append("        ex.getBindingResult().getAllErrors().forEach((err) -> {\n")
          .append("            String fieldName = ((FieldError) err).getField();\n")
          .append("            String errorMessage = err.getDefaultMessage();\n")
          .append("            validationErrors.put(fieldName, errorMessage);\n")
          .append("        });\n")
          .append("        error.put(\"validationErrors\", validationErrors);\n\n")
          .append("        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);\n")
          .append("    }\n\n")
          .append("    @ExceptionHandler(Exception.class)\n")
          .append("    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {\n")
          .append("        Map<String, Object> error = new HashMap<>();\n")
          .append("        error.put(\"timestamp\", LocalDateTime.now());\n")
          .append("        error.put(\"status\", HttpStatus.INTERNAL_SERVER_ERROR.value());\n")
          .append("        error.put(\"error\", \"Internal Server Error\");\n")
          .append("        error.put(\"message\", ex.getMessage());\n")
          .append("        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);\n")
          .append("    }\n")
          .append("}\n");
        return sb.toString();
    }

    private String generateSecurityConfig(ProjectMetadata metadata) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(metadata.getPackageName()).append(".config;\n\n")
          .append("import org.springframework.context.annotation.Bean;\n")
          .append("import org.springframework.context.annotation.Configuration;\n")
          .append("import org.springframework.security.config.annotation.web.builders.HttpSecurity;\n")
          .append("import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;\n")
          .append("import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;\n")
          .append("import org.springframework.security.web.SecurityFilterChain;\n\n")
          .append("@Configuration\n")
          .append("@EnableWebSecurity\n")
          .append("public class SecurityConfig {\n\n")
          .append("    @Bean\n")
          .append("    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {\n")
          .append("        http\n")
          .append("            .csrf(AbstractHttpConfigurer::disable)\n")
          .append("            .authorizeHttpRequests(auth -> auth\n")
          .append("                .anyRequest().permitAll()\n")
          .append("            );\n")
          .append("        return http.build();\n")
          .append("    }\n")
          .append("}\n");
        return sb.toString();
    }

    private String generateGettersSetters(List<ProjectMetadata.FieldMetadata> fields) {
        StringBuilder sb = new StringBuilder();
        for (ProjectMetadata.FieldMetadata field : fields) {
            String name = field.getName();
            String capName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            String type = field.getType();

            sb.append("    public ").append(type).append(" get").append(capName).append("() {\n")
              .append("        return this.").append(name).append(";\n")
              .append("    }\n\n")
              .append("    public void set").append(capName).append("(").append(type).append(" ").append(name).append(") {\n")
              .append("        this.").append(name).append(" = ").append(name).append(";\n")
              .append("    }\n\n");
        }
        return sb.toString();
    }

    private String generateNoArgConstructor(String className) {
        return "    public " + className + "() {\n    }\n\n";
    }

    private String generateAllArgConstructor(String className, List<ProjectMetadata.FieldMetadata> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("    public ").append(className).append("(");
        for (int i = 0; i < fields.size(); i++) {
            ProjectMetadata.FieldMetadata field = fields.get(i);
            sb.append(field.getType()).append(" ").append(field.getName());
            if (i < fields.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(") {\n");
        for (ProjectMetadata.FieldMetadata field : fields) {
            sb.append("        this.").append(field.getName()).append(" = ").append(field.getName()).append(";\n");
        }
        sb.append("    }\n\n");
        return sb.toString();
    }
}
