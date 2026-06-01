package com.notely.service;

import com.notely.dto.FlashcardDTO;
import com.notely.dto.QuizQuestionDTO;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AIService {
    String generateSummary(UUID noteId);
    List<FlashcardDTO> generateFlashcards(UUID noteId);
    List<QuizQuestionDTO> generateQuiz(UUID noteId);
    List<String> suggestTags(UUID noteId);
    String improveFormatting(UUID noteId);
    String chat(UUID noteId, String userMessage, List<Map<String, String>> history);
}
