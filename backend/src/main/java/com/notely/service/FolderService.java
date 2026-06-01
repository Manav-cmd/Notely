package com.notely.service;

import com.notely.dto.FolderRequest;
import com.notely.dto.FolderResponse;
import java.util.List;
import java.util.UUID;

public interface FolderService {
    FolderResponse createFolder(FolderRequest request);
    List<FolderResponse> getFoldersByNotebook(UUID notebookId);
    FolderResponse getFolderById(UUID folderId);
    FolderResponse updateFolder(UUID folderId, FolderRequest request);
    void deleteFolder(UUID folderId);
}
