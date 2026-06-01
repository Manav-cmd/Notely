package com.notely.service;

import com.notely.dto.WorkspaceRequest;
import com.notely.dto.WorkspaceResponse;
import com.notely.entity.User;
import com.notely.entity.Workspace;
import com.notely.exception.ResourceNotFoundException;
import com.notely.exception.BadRequestException;
import com.notely.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final UserService userService;

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceRequest request) {
        User currentUser = userService.getCurrentUserEntity();
        
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(currentUser)
                .build();
        
        Workspace saved = workspaceRepository.save(workspace);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getUserWorkspaces() {
        User currentUser = userService.getCurrentUserEntity();
        return workspaceRepository.findByOwnerId(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceById(UUID workspaceId) {
        Workspace workspace = getAndVerifyWorkspace(workspaceId);
        return mapToResponse(workspace);
    }

    @Override
    @Transactional
    public WorkspaceResponse updateWorkspace(UUID workspaceId, WorkspaceRequest request) {
        Workspace workspace = getAndVerifyWorkspace(workspaceId);
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        Workspace updated = workspaceRepository.save(workspace);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteWorkspace(UUID workspaceId) {
        Workspace workspace = getAndVerifyWorkspace(workspaceId);
        workspaceRepository.delete(workspace);
    }

    private Workspace getAndVerifyWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id: " + workspaceId));
        
        User currentUser = userService.getCurrentUserEntity();
        if (!workspace.getOwner().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You do not own this workspace");
        }
        return workspace;
    }

    private WorkspaceResponse mapToResponse(Workspace workspace) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .ownerId(workspace.getOwner().getId())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }
}
