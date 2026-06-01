package com.notely.controller;

import com.notely.dto.FlashcardDTO;
import com.notely.dto.QuizQuestionDTO;
import com.notely.dto.AIChatRequest;
import com.notely.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes/{noteId}/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIController {
    private final AIService aiService;

    @PostMapping("/summary")
    public ResponseEntity<Map<String, String>> getSummary(@PathVariable UUID noteId) {
        String summary = aiService.generateSummary(noteId);
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    @PostMapping("/flashcards")
    public ResponseEntity<List<FlashcardDTO>> getFlashcards(@PathVariable UUID noteId) {
        return ResponseEntity.ok(aiService.generateFlashcards(noteId));
    }

    @PostMapping("/quiz")
    public ResponseEntity<List<QuizQuestionDTO>> getQuiz(@PathVariable UUID noteId) {
        return ResponseEntity.ok(aiService.generateQuiz(noteId));
    }

    @PostMapping("/suggest-tags")
    public ResponseEntity<List<String>> getSuggestedTags(@PathVariable UUID noteId) {
        return ResponseEntity.ok(aiService.suggestTags(noteId));
    }

    @PostMapping("/format")
    public ResponseEntity<Map<String, String>> getFormattedContent(@PathVariable UUID noteId) {
        String formatted = aiService.improveFormatting(noteId);
        return ResponseEntity.ok(Map.of("formattedContent", formatted));
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithNote(
            @PathVariable UUID noteId,
            @RequestBody AIChatRequest chatRequest) {
        String reply = aiService.chat(noteId, chatRequest.getMessage(), chatRequest.getHistory());
        return ResponseEntity.ok(Map.of("response", reply));
    }
}

