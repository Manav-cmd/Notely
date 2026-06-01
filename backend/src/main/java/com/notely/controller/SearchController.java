package com.notely.controller;

import com.notely.dto.NoteResponse;
import com.notely.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<NoteResponse>> search(
            @RequestParam UUID workspaceId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID tagId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        return ResponseEntity.ok(searchService.search(workspaceId, query, tagId, startDate, endDate));
    }
}
