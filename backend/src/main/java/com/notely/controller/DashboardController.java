package com.notely.controller;

import com.notely.entity.User;
import com.notely.repository.NoteRepository;
import com.notely.repository.NotebookRepository;
import com.notely.repository.TagRepository;
import com.notely.repository.WorkspaceRepository;
import com.notely.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {
    private final NoteRepository noteRepository;
    private final WorkspaceRepository workspaceRepository;
    private final NotebookRepository notebookRepository;
    private final TagRepository tagRepository;
    private final UserService userService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        User user = userService.getCurrentUserEntity();
        UUID userId = user.getId();

        long totalNotes = noteRepository.countByOwnerId(userId);
        
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long createdThisWeek = noteRepository.countByOwnerIdAndCreatedAtAfter(userId, sevenDaysAgo);

        long totalWorkspaces = workspaceRepository.findByOwnerId(userId).size();
        
        long totalNotebooks = workspaceRepository.findByOwnerId(userId).stream()
                .mapToLong(ws -> notebookRepository.findByWorkspaceId(ws.getId()).size())
                .sum();

        List<Object[]> rawTags = tagRepository.findMostUsedTags();
        List<Map<String, Object>> mostUsedTags = new ArrayList<>();
        int countLimit = Math.min(rawTags.size(), 5);
        for (int i = 0; i < countLimit; i++) {
            Object[] row = rawTags.get(i);
            mostUsedTags.add(Map.of("name", row[0], "count", row[1]));
        }

        List<Map<String, Object>> activityHistory = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            
            long count = noteRepository.findByOwnerId(userId).stream()
                    .filter(n -> n.getCreatedAt().isAfter(start) && n.getCreatedAt().isBefore(end))
                    .count();

            activityHistory.add(Map.of("date", date.format(formatter), "notesCount", count));
        }

        String insight = "You have created " + createdThisWeek + " notes this week. ";
        if (createdThisWeek == 0) {
            insight += "It looks like a quiet week. Capture ideas, create flashcards, and build quizzes to start learning!";
        } else if (createdThisWeek < 3) {
            insight += "Good progress! Try organizing your notes with tags and folders to improve navigation.";
        } else {
            insight += "Excellent momentum! Use AI summaries to quickly review your notes and test your knowledge with AI Quizzes.";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalNotes", totalNotes);
        response.put("createdThisWeek", createdThisWeek);
        response.put("totalWorkspaces", totalWorkspaces);
        response.put("totalNotebooks", totalNotebooks);
        response.put("mostUsedTags", mostUsedTags);
        response.put("activityHistory", activityHistory);
        response.put("productivityInsight", insight);

        return ResponseEntity.ok(response);
    }
}
