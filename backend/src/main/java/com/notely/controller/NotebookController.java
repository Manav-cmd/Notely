package com.notely.controller;

import com.notely.dto.NotebookRequest;
import com.notely.dto.NotebookResponse;
import com.notely.service.NotebookService;
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
public class NotebookController {
    private final NotebookService notebookService;

    @PostMapping("/notebooks")
    public ResponseEntity<NotebookResponse> createNotebook(@Valid @RequestBody NotebookRequest request) {
        return ResponseEntity.ok(notebookService.createNotebook(request));
    }

    @GetMapping("/workspaces/{workspaceId}/notebooks")
    public ResponseEntity<List<NotebookResponse>> getNotebooksByWorkspace(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(notebookService.getNotebooksByWorkspace(workspaceId));
    }

    @GetMapping("/notebooks/{notebookId}")
    public ResponseEntity<NotebookResponse> getNotebookById(@PathVariable UUID notebookId) {
        return ResponseEntity.ok(notebookService.getNotebookById(notebookId));
    }

    @PutMapping("/notebooks/{notebookId}")
    public ResponseEntity<NotebookResponse> updateNotebook(
            @PathVariable UUID notebookId,
            @Valid @RequestBody NotebookRequest request) {
        return ResponseEntity.ok(notebookService.updateNotebook(notebookId, request));
    }

    @DeleteMapping("/notebooks/{notebookId}")
    public ResponseEntity<?> deleteNotebook(@PathVariable UUID notebookId) {
        notebookService.deleteNotebook(notebookId);
        return ResponseEntity.ok().build();
    }
}
