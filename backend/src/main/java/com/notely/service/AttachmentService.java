package com.notely.service;

import com.notely.dto.AttachmentResponse;
import com.notely.entity.Attachment;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

public interface AttachmentService {
    AttachmentResponse uploadAttachment(UUID noteId, MultipartFile file);
    List<AttachmentResponse> getAttachmentsByNote(UUID noteId);
    Attachment getAttachmentEntity(UUID attachmentId);
    void deleteAttachment(UUID noteId, UUID attachmentId);
}
