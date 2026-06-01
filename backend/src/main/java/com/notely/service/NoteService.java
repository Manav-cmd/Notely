package com.notely.service;

import com.notely.dto.NoteRequest;
import com.notely.dto.NoteResponse;
import com.notely.dto.RevisionResponse;
import com.notely.dto.SharedNoteRequest;
import com.notely.dto.SharedNoteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface NoteService {
    NoteResponse createNote(NoteRequest request);
    NoteResponse getNoteById(UUID noteId);
    Page<NoteResponse> getNotesByWorkspace(UUID workspaceId, boolean isArchived, Pageable pageable);
    List<NoteResponse> getNotesByNotebook(UUID notebookId);
    List<NoteResponse> getNotesByFolder(UUID folderId);
    NoteResponse updateNote(UUID noteId, NoteRequest request);
    void deleteNote(UUID noteId);
    
    NoteResponse archiveNote(UUID noteId);
    NoteResponse restoreNote(UUID noteId);
    NoteResponse togglePin(UUID noteId);
    NoteResponse toggleFavorite(UUID noteId);
    
    // Version History
    List<RevisionResponse> getNoteRevisions(UUID noteId);
    NoteResponse rollbackToRevision(UUID noteId, UUID revisionId);
    
    // Sharing & Collaboration
    SharedNoteResponse shareNote(UUID noteId, SharedNoteRequest request);
    void revokeShare(UUID noteId, UUID shareId);
    List<SharedNoteResponse> getNoteShares(UUID noteId);
    List<NoteResponse> getSharedNotesWithMe();
}
