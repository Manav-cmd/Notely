package com.notely.service;

import com.notely.dto.CommentRequest;
import com.notely.dto.CommentResponse;
import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentResponse addComment(UUID noteId, CommentRequest request);
    List<CommentResponse> getCommentsByNote(UUID noteId);
    void deleteComment(UUID noteId, UUID commentId);
}
