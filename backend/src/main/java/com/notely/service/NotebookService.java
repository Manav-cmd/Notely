package com.notely.service;

import com.notely.dto.NotebookRequest;
import com.notely.dto.NotebookResponse;
import java.util.List;
import java.util.UUID;

public interface NotebookService {
    NotebookResponse createNotebook(NotebookRequest request);
    List<NotebookResponse> getNotebooksByWorkspace(UUID workspaceId);
    NotebookResponse getNotebookById(UUID notebookId);
    NotebookResponse updateNotebook(UUID notebookId, NotebookRequest request);
    void deleteNotebook(UUID notebookId);
}
