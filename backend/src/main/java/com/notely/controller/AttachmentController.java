package com.notely.controller;

import com.notely.dto.AttachmentResponse;
import com.notely.entity.Attachment;
import com.notely.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes/{noteId}/attachments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttachmentController {
    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable UUID noteId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(attachmentService.uploadAttachment(noteId, file));
    }

    @GetMapping
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsByNote(@PathVariable UUID noteId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByNote(noteId));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable UUID noteId,
            @PathVariable UUID attachmentId) {
        Attachment attachment = attachmentService.getAttachmentEntity(attachmentId);
        File file = new File(attachment.getStoragePath());
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(
            @PathVariable UUID noteId,
            @PathVariable UUID attachmentId) {
        attachmentService.deleteAttachment(noteId, attachmentId);
        return ResponseEntity.ok().build();
    }
}
