package com.notely.service;

import com.notely.dto.FolderRequest;
import com.notely.dto.FolderResponse;
import com.notely.entity.Folder;
import com.notely.entity.Notebook;
import com.notely.entity.User;
import com.notely.entity.Workspace;
import com.notely.exception.ResourceNotFoundException;
import com.notely.exception.BadRequestException;
import com.notely.repository.FolderRepository;
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
public class FolderServiceImpl implements FolderService {
    private final FolderRepository folderRepository;
    private final NotebookRepository notebookRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserService userService;

    @Override
    @Transactional
    public FolderResponse createFolder(FolderRequest request) {
        Notebook notebook = getAndVerifyNotebook(request.getNotebookId());
        
        Folder parentFolder = null;
        if (request.getParentFolderId() != null) {
            parentFolder = folderRepository.findById(request.getParentFolderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent folder not found"));
        }

        Folder folder = Folder.builder()
                .name(request.getName())
                .notebook(notebook)
                .parentFolder(parentFolder)
                .build();
        
        Folder saved = folderRepository.save(folder);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> getFoldersByNotebook(UUID notebookId) {
        getAndVerifyNotebook(notebookId);
        return folderRepository.findByNotebookId(notebookId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FolderResponse getFolderById(UUID folderId) {
        Folder folder = getAndVerifyFolder(folderId);
        return mapToResponse(folder);
    }

    @Override
    @Transactional
    public FolderResponse updateFolder(UUID folderId, FolderRequest request) {
        Folder folder = getAndVerifyFolder(folderId);
        folder.setName(request.getName());
        Folder updated = folderRepository.save(folder);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteFolder(UUID folderId) {
        Folder folder = getAndVerifyFolder(folderId);
        folderRepository.delete(folder);
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

    private Folder getAndVerifyFolder(UUID folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + folderId));
        getAndVerifyNotebook(folder.getNotebook().getId());
        return folder;
    }

    private FolderResponse mapToResponse(Folder folder) {
        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .notebookId(folder.getNotebook().getId())
                .parentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getId() : null)
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
}
