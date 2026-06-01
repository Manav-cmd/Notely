package com.notely.controller;

import com.notely.dto.CommentRequest;
import com.notely.dto.CommentResponse;
import com.notely.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes/{noteId}/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID noteId,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.addComment(noteId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByNote(@PathVariable UUID noteId) {
        return ResponseEntity.ok(commentService.getCommentsByNote(noteId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable UUID noteId,
            @PathVariable UUID commentId) {
        commentService.deleteComment(noteId, commentId);
        return ResponseEntity.ok().build();
    }
}
