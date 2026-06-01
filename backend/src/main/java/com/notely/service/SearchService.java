package com.notely.service;

import com.notely.dto.NoteResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SearchService {
    List<NoteResponse> search(UUID workspaceId, String query, UUID tagId, LocalDateTime startDate, LocalDateTime endDate);
}
