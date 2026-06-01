package com.notely.service;

import com.notely.dto.AttachmentResponse;
import com.notely.entity.Attachment;
import com.notely.entity.Note;
import com.notely.entity.SharedNote;
import com.notely.entity.User;
import com.notely.entity.SharePermission;
import com.notely.exception.ResourceNotFoundException;
import com.notely.exception.BadRequestException;
import com.notely.repository.AttachmentRepository;
import com.notely.repository.NoteRepository;
import com.notely.repository.SharedNoteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttachmentServiceImpl implements AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final NoteRepository noteRepository;
    private final SharedNoteRepository sharedNoteRepository;
    private final UserService userService;
    private final Path fileStorageLocation;

    public AttachmentServiceImpl(
            AttachmentRepository attachmentRepository,
            NoteRepository noteRepository,
            SharedNoteRepository sharedNoteRepository,
            UserService userService,
            @Value("${app.file.storage-dir}") String storageDir) {
        this.attachmentRepository = attachmentRepository;
        this.noteRepository = noteRepository;
        this.sharedNoteRepository = sharedNoteRepository;
        this.userService = userService;
        this.fileStorageLocation = Paths.get(storageDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new BadRequestException("Could not create the directory where the uploaded files will be stored.");
        }
    }

    @Override
    @Transactional
    public AttachmentResponse uploadAttachment(UUID noteId, MultipartFile file) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.EDIT);

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.contains("..")) {
            throw new BadRequestException("Sorry! Filename contains invalid path sequence " + originalFileName);
        }

        String fileExtension = "";
        int index = originalFileName.lastIndexOf('.');
        if (index > 0) {
            fileExtension = originalFileName.substring(index);
        }

        String storedFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = Attachment.builder()
                    .fileName(originalFileName)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .storagePath(targetLocation.toString())
                    .note(note)
                    .build();

            Attachment saved = attachmentRepository.save(attachment);
            return mapToResponse(saved);

        } catch (IOException ex) {
            throw new BadRequestException("Could not store file " + originalFileName + ". Please try again!");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByNote(UUID noteId) {
        getAndVerifyNoteAccess(noteId, SharePermission.READ);
        return attachmentRepository.findByNoteId(noteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Attachment getAttachmentEntity(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
        getAndVerifyNoteAccess(attachment.getNote().getId(), SharePermission.READ);
        return attachment;
    }

    @Override
    @Transactional
    public void deleteAttachment(UUID noteId, UUID attachmentId) {
        getAndVerifyNoteAccess(noteId, SharePermission.EDIT);
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        if (!attachment.getNote().getId().equals(noteId)) {
            throw new BadRequestException("Attachment does not belong to this note");
        }

        try {
            Path filePath = Paths.get(attachment.getStoragePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log warning but continue deleting DB record
        }

        attachmentRepository.delete(attachment);
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

    private AttachmentResponse mapToResponse(Attachment attachment) {
        String downloadUrl = "/api/notes/" + attachment.getNote().getId() + "/attachments/" + attachment.getId() + "/download";
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .downloadUrl(downloadUrl)
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
