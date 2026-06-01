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
public class FolderResponse {
    private UUID id;
    private String name;
    private UUID notebookId;
    private UUID parentFolderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
