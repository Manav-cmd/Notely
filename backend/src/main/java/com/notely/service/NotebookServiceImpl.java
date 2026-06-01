package com.notely.service;

import com.notely.dto.NotebookRequest;
import com.notely.dto.NotebookResponse;
import com.notely.entity.Notebook;
import com.notely.entity.User;
import com.notely.entity.Workspace;
import com.notely.exception.ResourceNotFoundException;
import com.notely.exception.BadRequestException;
import com.notely.repository.NotebookRepository;
import com.notely.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotebookServiceImpl implements NotebookService {
    private final NotebookRepository notebookRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserService userService;

    @Override
    @Transactional
    public NotebookResponse createNotebook(NotebookRequest request) {
        Workspace workspace = getAndVerifyWorkspace(request.getWorkspaceId());
        
        Notebook notebook = Notebook.builder()
                .name(request.getName())
                .description(request.getDescription())
                .workspace(workspace)
                .build();
        
        Notebook saved = notebookRepository.save(notebook);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotebookResponse> getNotebooksByWorkspace(UUID workspaceId) {
        getAndVerifyWorkspace(workspaceId);
        return notebookRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NotebookResponse getNotebookById(UUID notebookId) {
        Notebook notebook = getAndVerifyNotebook(notebookId);
        return mapToResponse(notebook);
    }

    @Override
    @Transactional
    public NotebookResponse updateNotebook(UUID notebookId, NotebookRequest request) {
        Notebook notebook = getAndVerifyNotebook(notebookId);
        notebook.setName(request.getName());
        notebook.setDescription(request.getDescription());
        Notebook updated = notebookRepository.save(notebook);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteNotebook(UUID notebookId) {
        Notebook notebook = getAndVerifyNotebook(notebookId);
        notebookRepository.delete(notebook);
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

    private Notebook getAndVerifyNotebook(UUID notebookId) {
        Notebook notebook = notebookRepository.findById(notebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook not found with id: " + notebookId));
        getAndVerifyWorkspace(notebook.getWorkspace().getId());
        return notebook;
    }

    private NotebookResponse mapToResponse(Notebook notebook) {
        return NotebookResponse.builder()
                .id(notebook.getId())
                .name(notebook.getName())
                .description(notebook.getDescription())
                .workspaceId(notebook.getWorkspace().getId())
                .createdAt(notebook.getCreatedAt())
                .updatedAt(notebook.getUpdatedAt())
                .build();
    }
}
