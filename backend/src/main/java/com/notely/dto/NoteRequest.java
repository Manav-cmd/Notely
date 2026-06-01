package com.notely.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class NoteRequest {
    @NotBlank(message = "Note title is required")
    private String title;
    
    private String content;
    
    @NotNull(message = "Workspace ID is required")
    private UUID workspaceId;
    
    private UUID notebookId;
    
    private UUID folderId;
    
    private boolean isPinned;
    
    private boolean isFavorite;
    
    private boolean isArchived;
    
    private List<String> tagNames;
}
