package com.notely.dto;

import com.notely.entity.SharePermission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedNoteResponse {
    private UUID id;
    private UUID noteId;
    private String noteTitle;
    private String sharedWithEmail;
    private String sharedWithUsername;
    private SharePermission permission;
    private LocalDateTime createdAt;
}
