package com.notely.service;

import com.notely.dto.CommentRequest;
import com.notely.dto.CommentResponse;
import com.notely.entity.Comment;
import com.notely.entity.Note;
import com.notely.entity.SharedNote;
import com.notely.entity.User;
import com.notely.entity.SharePermission;
import com.notely.exception.ResourceNotFoundException;
import com.notely.exception.BadRequestException;
import com.notely.repository.CommentRepository;
import com.notely.repository.NoteRepository;
import com.notely.repository.SharedNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final NoteRepository noteRepository;
    private final SharedNoteRepository sharedNoteRepository;
    private final UserService userService;

    @Override
    @Transactional
    public CommentResponse addComment(UUID noteId, CommentRequest request) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.READ);
        User currentUser = userService.getCurrentUserEntity();

        Comment comment = Comment.builder()
                .note(note)
                .user(currentUser)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByNote(UUID noteId) {
        getAndVerifyNoteAccess(noteId, SharePermission.READ);
        return commentRepository.findByNoteIdOrderByCreatedAtAsc(noteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(UUID noteId, UUID commentId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.READ);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getNote().getId().equals(noteId)) {
            throw new BadRequestException("Comment does not match note ID");
        }

        User currentUser = userService.getCurrentUserEntity();

        if (comment.getUser().getId().equals(currentUser.getId()) || note.getOwner().getId().equals(currentUser.getId())) {
            commentRepository.delete(comment);
        } else {
            throw new BadRequestException("Unauthorized to delete this comment");
        }
    }

    private Note getAndVerifyNoteAccess(UUID noteId, SharePermission requiredPermission) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));
        
        User currentUser = userService.getCurrentUserEntity();

        if (note.getOwner().getId().equals(currentUser.getId())) {
            return note;
        }

        Optional<SharedNote> sharedOpt = sharedNoteRepository.findByNoteIdAndSharedWithId(noteId, currentUser.getId());
        if (sharedOpt.isPresent()) {
            SharedNote shared = sharedOpt.get();
            if (requiredPermission == SharePermission.READ || shared.getPermission() == SharePermission.EDIT) {
                return note;
            }
        }
        throw new BadRequestException("Access denied for note");
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .noteId(comment.getNote().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
