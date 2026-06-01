package com.notely.service;

import com.notely.dto.*;
import com.notely.entity.*;
import com.notely.exception.*;
import com.notely.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {
    private final NoteRepository noteRepository;
    private final WorkspaceRepository workspaceRepository;
    private final NotebookRepository notebookRepository;
    private final FolderRepository folderRepository;
    private final TagRepository tagRepository;
    private final RevisionRepository revisionRepository;
    private final SharedNoteRepository sharedNoteRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public NoteResponse createNote(NoteRequest request) {
        Workspace workspace = getAndVerifyWorkspace(request.getWorkspaceId());
        User currentUser = userService.getCurrentUserEntity();

        Notebook notebook = null;
        if (request.getNotebookId() != null) {
            notebook = notebookRepository.findById(request.getNotebookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Notebook not found"));
        }

        Folder folder = null;
        if (request.getFolderId() != null) {
            folder = folderRepository.findById(request.getFolderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
        }

        Set<Tag> tags = resolveTags(request.getTagNames());

        Note note = Note.builder()
                .title(request.getTitle())
                .content(request.getContent() != null ? request.getContent() : "")
                .isArchived(request.isArchived())
                .isPinned(request.isPinned())
                .isFavorite(request.isFavorite())
                .owner(currentUser)
                .workspace(workspace)
                .notebook(notebook)
                .folder(folder)
                .tags(tags)
                .build();

        Note savedNote = noteRepository.save(note);
        
        createRevision(savedNote, 1, currentUser);

        return mapToResponse(savedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse getNoteById(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.READ);
        return mapToResponse(note);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponse> getNotesByWorkspace(UUID workspaceId, boolean isArchived, Pageable pageable) {
        getAndVerifyWorkspace(workspaceId);
        return noteRepository.findByWorkspaceIdAndIsArchived(workspaceId, isArchived, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesByNotebook(UUID notebookId) {
        Notebook notebook = notebookRepository.findById(notebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook not found"));
        getAndVerifyWorkspace(notebook.getWorkspace().getId());
        return noteRepository.findByNotebookIdAndIsArchived(notebookId, false).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesByFolder(UUID folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
        getAndVerifyWorkspace(folder.getNotebook().getWorkspace().getId());
        return noteRepository.findByFolderIdAndIsArchived(folderId, false).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteResponse updateNote(UUID noteId, NoteRequest request) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.EDIT);
        User currentUser = userService.getCurrentUserEntity();

        boolean contentChanged = !Objects.equals(note.getContent(), request.getContent()) || 
                                 !Objects.equals(note.getTitle(), request.getTitle());

        note.setTitle(request.getTitle());
        note.setContent(request.getContent() != null ? request.getContent() : "");
        note.setPinned(request.isPinned());
        note.setFavorite(request.isFavorite());
        note.setArchived(request.isArchived());

        if (request.getNotebookId() != null) {
            Notebook notebook = notebookRepository.findById(request.getNotebookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Notebook not found"));
            note.setNotebook(notebook);
        } else {
            note.setNotebook(null);
        }

        if (request.getFolderId() != null) {
            Folder folder = folderRepository.findById(request.getFolderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
            note.setFolder(folder);
        } else {
            note.setFolder(null);
        }

        if (request.getTagNames() != null) {
            note.setTags(resolveTags(request.getTagNames()));
        }

        Note updatedNote = noteRepository.save(note);

        if (contentChanged) {
            List<Revision> revisions = revisionRepository.findByNoteIdOrderByVersionNumberDesc(noteId);
            int nextVersion = revisions.isEmpty() ? 1 : revisions.get(0).getVersionNumber() + 1;
            createRevision(updatedNote, nextVersion, currentUser);
        }

        return mapToResponse(updatedNote);
    }

    @Override
    @Transactional
    public void deleteNote(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.EDIT);
        noteRepository.delete(note);
    }

    @Override
    @Transactional
    public NoteResponse archiveNote(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.EDIT);
        note.setArchived(true);
        return mapToResponse(noteRepository.save(note));
    }

    @Override
    @Transactional
    public NoteResponse restoreNote(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.EDIT);
        note.setArchived(false);
        return mapToResponse(noteRepository.save(note));
    }

    @Override
    @Transactional
    public NoteResponse togglePin(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.EDIT);
        note.setPinned(!note.isPinned());
        return mapToResponse(noteRepository.save(note));
    }

    @Override
    @Transactional
    public NoteResponse toggleFavorite(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.EDIT);
        note.setFavorite(!note.isFavorite());
        return mapToResponse(noteRepository.save(note));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RevisionResponse> getNoteRevisions(UUID noteId) {
        getAndVerifyNoteAccess(noteId, SharePermission.READ);
        return revisionRepository.findByNoteIdOrderByVersionNumberDesc(noteId).stream()
                .map(this::mapToRevisionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteResponse rollbackToRevision(UUID noteId, UUID revisionId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.EDIT);
        Revision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found"));
        
        if (!revision.getNote().getId().equals(noteId)) {
            throw new BadRequestException("Revision does not belong to this note");
        }

        note.setTitle(revision.getTitle());
        note.setContent(revision.getContent());
        Note updated = noteRepository.save(note);

        List<Revision> revisions = revisionRepository.findByNoteIdOrderByVersionNumberDesc(noteId);
        int nextVersion = revisions.isEmpty() ? 1 : revisions.get(0).getVersionNumber() + 1;
        createRevision(updated, nextVersion, userService.getCurrentUserEntity());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public SharedNoteResponse shareNote(UUID noteId, SharedNoteRequest request) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.EDIT);
        User recipient = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (recipient.getId().equals(note.getOwner().getId())) {
            throw new BadRequestException("You cannot share a note with yourself (owner)");
        }

        if (sharedNoteRepository.existsByNoteIdAndSharedWithId(noteId, recipient.getId())) {
            SharedNote existing = sharedNoteRepository.findByNoteIdAndSharedWithId(noteId, recipient.getId()).get();
            existing.setPermission(request.getPermission());
            return mapToSharedResponse(sharedNoteRepository.save(existing));
        }

        SharedNote sharedNote = SharedNote.builder()
                .note(note)
                .sharedWith(recipient)
                .permission(request.getPermission())
                .build();

        return mapToSharedResponse(sharedNoteRepository.save(sharedNote));
    }

    @Override
    @Transactional
    public void revokeShare(UUID noteId, UUID shareId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.EDIT);
        SharedNote sharedNote = sharedNoteRepository.findById(shareId)
                .orElseThrow(() -> new ResourceNotFoundException("Share entry not found"));
        
        if (!sharedNote.getNote().getId().equals(noteId)) {
            throw new BadRequestException("Share record does not match this note");
        }
        
        sharedNoteRepository.delete(sharedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SharedNoteResponse> getNoteShares(UUID noteId) {
        getAndVerifyNoteAccess(noteId, SharePermission.READ);
        return sharedNoteRepository.findByNoteId(noteId).stream()
                .map(this::mapToSharedResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getSharedNotesWithMe() {
        User currentUser = userService.getCurrentUserEntity();
        return sharedNoteRepository.findBySharedWithId(currentUser.getId()).stream()
                .map(sn -> mapToResponse(sn.getNote()))
                .collect(Collectors.toList());
    }

    private Workspace getAndVerifyWorkspace(UUID workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        User currentUser = userService.getCurrentUserEntity();
        if (!workspace.getOwner().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Workspace access denied");
        }
        return workspace;
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

    private Set<Tag> resolveTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            String cleanName = name.trim().toLowerCase();
            if (cleanName.isEmpty()) continue;
            Tag tag = tagRepository.findByName(cleanName)
                    .orElseGet(() -> {
                        String[] colors = {"#ff5f5f", "#5fff5f", "#5f5fff", "#ffcf5f", "#ff5fcf", "#5ffffc"};
                        String color = colors[new Random().nextInt(colors.length)];
                        return tagRepository.save(Tag.builder().name(cleanName).color(color).build());
                    });
            tags.add(tag);
        }
        return tags;
    }

    private void createRevision(Note note, int version, User user) {
        Revision revision = Revision.builder()
                .note(note)
                .title(note.getTitle())
                .content(note.getContent())
                .versionNumber(version)
                .updatedBy(user)
                .build();
        revisionRepository.save(revision);
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

    private RevisionResponse mapToRevisionResponse(Revision revision) {
        return RevisionResponse.builder()
                .id(revision.getId())
                .versionNumber(revision.getVersionNumber())
                .title(revision.getTitle())
                .content(revision.getContent())
                .updatedByUsername(revision.getUpdatedBy().getUsername())
                .createdAt(revision.getCreatedAt())
                .build();
    }

    private SharedNoteResponse mapToSharedResponse(SharedNote sn) {
        return SharedNoteResponse.builder()
                .id(sn.getId())
                .noteId(sn.getNote().getId())
                .noteTitle(sn.getNote().getTitle())
                .sharedWithEmail(sn.getSharedWith().getEmail())
                .sharedWithUsername(sn.getSharedWith().getUsername())
                .permission(sn.getPermission())
                .createdAt(sn.getCreatedAt())
                .build();
    }
}
