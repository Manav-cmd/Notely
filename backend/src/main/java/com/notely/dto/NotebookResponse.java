package com.notely.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotebookResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID workspaceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
