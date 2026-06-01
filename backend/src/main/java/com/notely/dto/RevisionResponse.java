package com.notely.dto;

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
public class RevisionResponse {
    private UUID id;
    private int versionNumber;
    private String title;
    private String content;
    private String updatedByUsername;
    private LocalDateTime createdAt;
}
