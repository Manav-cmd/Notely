package com.notely.service;

import com.notely.dto.NoteResponse;
import com.notely.dto.TagResponse;
import com.notely.entity.Note;
import com.notely.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final NoteRepository noteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> search(UUID workspaceId, String query, UUID tagId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Note> notes;
        
        if (query != null && !query.trim().isEmpty()) {
            notes = noteRepository.searchNotes(workspaceId, false, query.trim());
        } else if (tagId != null) {
            notes = noteRepository.findByWorkspaceIdAndIsArchivedAndTagId(workspaceId, false, tagId);
        } else if (startDate != null && endDate != null) {
            notes = noteRepository.findByWorkspaceIdAndIsArchivedAndDateRange(workspaceId, false, startDate, endDate);
        } else {
            notes = noteRepository.findByWorkspaceIdAndIsArchived(workspaceId, false);
        }

        return notes.stream()
                .filter(n -> tagId == null || n.getTags().stream().anyMatch(t -> t.getId().equals(tagId)))
                .filter(n -> startDate == null || endDate == null || (n.getCreatedAt().isAfter(startDate) && n.getCreatedAt().isBefore(endDate)))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private NoteResponse mapToResponse(Note note) {
        List<TagResponse> tags = note.getTags().stream()
                .map(t -> TagResponse.builder().id(t.getId()).name(t.getName()).color(t.getColor()).build())
                .collect(Collectors.toList());

        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .isArchived(note.isArchived())
                .isPinned(note.isPinned())
                .isFavorite(note.isFavorite())
                .ownerId(note.getOwner().getId())
                .ownerUsername(note.getOwner().getUsername())
                .workspaceId(note.getWorkspace().getId())
                .notebookId(note.getNotebook() != null ? note.getNotebook().getId() : null)
                .folderId(note.getFolder() != null ? note.getFolder().getId() : null)
                .tags(tags)
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
