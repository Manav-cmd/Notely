package com.notely.service;

import com.notely.dto.WorkspaceRequest;
import com.notely.dto.WorkspaceResponse;
import java.util.List;
import java.util.UUID;

public interface WorkspaceService {
    WorkspaceResponse createWorkspace(WorkspaceRequest request);
    List<WorkspaceResponse> getUserWorkspaces();
    WorkspaceResponse getWorkspaceById(UUID workspaceId);
    WorkspaceResponse updateWorkspace(UUID workspaceId, WorkspaceRequest request);
    void deleteWorkspace(UUID workspaceId);
}
