package com.notely.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {
    private UUID id;
    private String title;
    private String content;
    private boolean isArchived;
    private boolean isPinned;
    private boolean isFavorite;
    private UUID ownerId;
    private String ownerUsername;
    private UUID workspaceId;
    private UUID notebookId;
    private UUID folderId;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
