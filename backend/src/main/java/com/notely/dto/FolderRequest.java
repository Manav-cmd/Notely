package com.notely.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class FolderRequest {
    @NotBlank(message = "Folder name is required")
    private String name;
    @NotNull(message = "Notebook ID is required")
    private UUID notebookId;
    private UUID parentFolderId;
}
