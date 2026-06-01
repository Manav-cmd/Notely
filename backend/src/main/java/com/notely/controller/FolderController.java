package com.notely.controller;

import com.notely.dto.FolderRequest;
import com.notely.dto.FolderResponse;
import com.notely.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FolderController {
    private final FolderService folderService;

    @PostMapping("/folders")
    public ResponseEntity<FolderResponse> createFolder(@Valid @RequestBody FolderRequest request) {
        return ResponseEntity.ok(folderService.createFolder(request));
    }

    @GetMapping("/notebooks/{notebookId}/folders")
    public ResponseEntity<List<FolderResponse>> getFoldersByNotebook(@PathVariable UUID notebookId) {
        return ResponseEntity.ok(folderService.getFoldersByNotebook(notebookId));
    }

    @GetMapping("/folders/{folderId}")
    public ResponseEntity<FolderResponse> getFolderById(@PathVariable UUID folderId) {
        return ResponseEntity.ok(folderService.getFolderById(folderId));
    }

    @PutMapping("/folders/{folderId}")
    public ResponseEntity<FolderResponse> updateFolder(
            @PathVariable UUID folderId,
            @Valid @RequestBody FolderRequest request) {
        return ResponseEntity.ok(folderService.updateFolder(folderId, request));
    }

    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<?> deleteFolder(@PathVariable UUID folderId) {
        folderService.deleteFolder(folderId);
        return ResponseEntity.ok().build();
    }
}
