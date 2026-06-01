package com.notely.controller;

import com.notely.dto.*;
import com.notely.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NoteController {
    private final NoteService noteService;

    @PostMapping("/notes")
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteRequest request) {
        return ResponseEntity.ok(noteService.createNote(request));
    }

    @GetMapping("/notes/{noteId}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable UUID noteId) {
        return ResponseEntity.ok(noteService.getNoteById(noteId));
    }

    @GetMapping("/workspaces/{workspaceId}/notes")
    public ResponseEntity<Page<NoteResponse>> getNotesByWorkspace(
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "false") boolean archived) {

        Sort sort = Sort.by(direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(noteService.getNotesByWorkspace(workspaceId, archived, pageable));
    }

    @GetMapping("/notebooks/{notebookId}/notes")
    public ResponseEntity<List<NoteResponse>> getNotesByNotebook(@PathVariable UUID notebookId) {
        return ResponseEntity.ok(noteService.getNotesByNotebook(notebookId));
    }

    @GetMapping("/folders/{folderId}/notes")
    public ResponseEntity<List<NoteResponse>> getNotesByFolder(@PathVariable UUID folderId) {
        return ResponseEntity.ok(noteService.getNotesByFolder(folderId));
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable UUID noteId,
            @Valid @RequestBody NoteRequest request) {
        return ResponseEntity.ok(noteService.updateNote(noteId, request));
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable UUID noteId) {
        noteService.deleteNote(noteId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notes/{noteId}/archive")
    public ResponseEntity<NoteResponse> archiveNote(@PathVariable UUID noteId) {
        return ResponseEntity.ok(noteService.archiveNote(noteId));
    }

    @PutMapping("/notes/{noteId}/restore")
    public ResponseEntity<NoteResponse> restoreNote(@PathVariable UUID noteId) {
        return ResponseEntity.ok(noteService.restoreNote(noteId));
    }

    @PutMapping("/notes/{noteId}/pin")
    public ResponseEntity<NoteResponse> togglePin(@PathVariable UUID noteId) {
        return ResponseEntity.ok(noteService.togglePin(noteId));
    }

    @PutMapping("/notes/{noteId}/favorite")
    public ResponseEntity<NoteResponse> toggleFavorite(@PathVariable UUID noteId) {
        return ResponseEntity.ok(noteService.toggleFavorite(noteId));
    }

    // Version History
    @GetMapping("/notes/{noteId}/revisions")
    public ResponseEntity<List<RevisionResponse>> getNoteRevisions(@PathVariable UUID noteId) {
        return ResponseEntity.ok(noteService.getNoteRevisions(noteId));
    }

    @PostMapping("/notes/{noteId}/revisions/{revisionId}/rollback")
    public ResponseEntity<NoteResponse> rollbackToRevision(
            @PathVariable UUID noteId,
            @PathVariable UUID revisionId) {
        return ResponseEntity.ok(noteService.rollbackToRevision(noteId, revisionId));
    }

    // Sharing & Collaboration
    @PostMapping("/notes/{noteId}/shares")
    public ResponseEntity<SharedNoteResponse> shareNote(
            @PathVariable UUID noteId,
            @Valid @RequestBody SharedNoteRequest request) {
        return ResponseEntity.ok(noteService.shareNote(noteId, request));
    }

    @DeleteMapping("/notes/{noteId}/shares/{shareId}")
    public ResponseEntity<?> revokeShare(
            @PathVariable UUID noteId,
            @PathVariable UUID shareId) {
        noteService.revokeShare(noteId, shareId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notes/{noteId}/shares")
    public ResponseEntity<List<SharedNoteResponse>> getNoteShares(@PathVariable UUID noteId) {
        return ResponseEntity.ok(noteService.getNoteShares(noteId));
    }

    @GetMapping("/notes/shared-with-me")
    public ResponseEntity<List<NoteResponse>> getSharedNotesWithMe() {
        return ResponseEntity.ok(noteService.getSharedNotesWithMe());
    }
}
