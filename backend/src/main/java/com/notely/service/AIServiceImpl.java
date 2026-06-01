package com.notely.service;

import com.notely.dto.FlashcardDTO;
import com.notely.dto.QuizQuestionDTO;
import com.notely.entity.Note;
import com.notely.entity.SharedNote;
import com.notely.entity.User;
import com.notely.entity.SharePermission;
import com.notely.exception.ResourceNotFoundException;
import com.notely.exception.BadRequestException;
import com.notely.repository.NoteRepository;
import com.notely.repository.SharedNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    private final NoteRepository noteRepository;
    private final SharedNoteRepository sharedNoteRepository;
    private final UserService userService;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Executes a chat model call using Spring AI with a system prompt and user content.
     */
    private Optional<String> callSpringAi(String systemPrompt, String userPrompt) {
        try {
            SystemMessage systemMessage = new SystemMessage(systemPrompt);
            UserMessage userMessage = new UserMessage(userPrompt);
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
            
            ChatResponse chatResponse = chatClient.call(prompt);
            if (chatResponse != null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                String responseText = chatResponse.getResult().getOutput().getContent();
                if (responseText != null && !responseText.trim().isEmpty()) {
                    return Optional.of(responseText.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Spring AI call failed, falling back to mock: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Helper to strip markdown JSON code block formatting if returned by the model.
     */
    private String cleanJsonSnippet(String raw) {
        if (raw == null) return "";
        String cleaned = raw.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public String generateSummary(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.READ);
        String text = note.getContent();
        if (text == null || text.trim().isEmpty()) {
            return "This note is empty. Please add content before generating a summary.";
        }

        String systemPrompt = "You are Notely AI, an executive summary assistant. Summarize the user's note content into a beautiful executive summary. Organize it with a brief overview paragraph followed by a clean bulleted list of 3-4 high-impact key takeaways. Use rich markdown and emojis for formatting. Do not include any greeting or conversational fluff.";
        Optional<String> aiRes = callSpringAi(systemPrompt, text);
        if (aiRes.isPresent()) {
            return aiRes.get();
        }

        // Fallback: Rule-based generator
        String cleanText = text.replaceAll("#+\\s+", "").replaceAll("\\*+", "").replaceAll("`+", "");
        String[] lines = cleanText.split("\\n");
        StringBuilder sb = new StringBuilder();
        int sentencesCount = 0;
        
        sb.append("### AI Summary of \"").append(note.getTitle()).append("\" (Fallback)\n\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.length() > 15) {
                sb.append("• ").append(trimmed).append("\n");
                sentencesCount++;
                if (sentencesCount >= 4) break;
            }
        }
        if (sentencesCount == 0) {
            sb.append("The note contains short fragments: \"").append(cleanText).append("\"");
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashcardDTO> generateFlashcards(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.READ);
        String text = note.getContent();
        
        if (text == null || text.trim().isEmpty()) {
            return List.of(new FlashcardDTO("Empty Note", "Please write some concepts inside your note first!"));
        }

        String systemPrompt = "You are an educational assistant. Generate exactly 3-4 study flashcards (Question/Answer format) based on the user's note. Return them as a JSON array of objects, where each object has 'front' (the question) and 'back' (the answer). Return ONLY valid raw JSON, no markdown code blocks, no backticks, no wrap.";
        Optional<String> aiRes = callSpringAi(systemPrompt, text);
        if (aiRes.isPresent()) {
            try {
                String cleanJson = cleanJsonSnippet(aiRes.get());
                List<FlashcardDTO> cards = objectMapper.readValue(cleanJson, new TypeReference<List<FlashcardDTO>>() {});
                if (cards != null && !cards.isEmpty()) {
                    return cards;
                }
            } catch (Exception e) {
                System.err.println("Failed to parse flashcards JSON: " + e.getMessage());
            }
        }

        // Fallback: regex-based concept generator
        List<FlashcardDTO> cards = new ArrayList<>();
        Pattern pattern = Pattern.compile("##?\\s+([^\\n]+)\\n([^#]*)");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find() && cards.size() < 4) {
            String concept = matcher.group(1).trim();
            String definition = matcher.group(2).trim();
            if (definition.length() > 15) {
                if (definition.length() > 150) {
                    definition = definition.substring(0, 147) + "...";
                }
                cards.add(new FlashcardDTO("What is the concept of " + concept + "?", definition));
            }
        }

        if (cards.isEmpty()) {
            cards.add(new FlashcardDTO("What is the main topic of " + note.getTitle() + "?", 
                    "This note discusses " + note.getTitle() + " and related knowledge definitions."));
            cards.add(new FlashcardDTO("How do you apply the concepts from " + note.getTitle() + "?", 
                    "Implement the notes by revising the terms and tagging them correctly inside Notely."));
        }
        return cards;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizQuestionDTO> generateQuiz(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.READ);
        String text = note.getContent();

        if (text == null || text.trim().isEmpty()) {
            return List.of(new QuizQuestionDTO("Which app are you using right now?", 
                    List.of("Notely", "Wordpad", "Sticky Notes", "Evernote"), "Notely"));
        }

        String systemPrompt = "You are an educational assistant. Generate exactly 2-3 multiple-choice quiz questions based on the user's note. Return them as a JSON array of objects, where each object has 'question' (String), 'options' (array of exactly 4 strings), and 'correctAnswer' (string, matching one of the options). Return ONLY valid raw JSON, no markdown code blocks, no backticks, no wrap.";
        Optional<String> aiRes = callSpringAi(systemPrompt, text);
        if (aiRes.isPresent()) {
            try {
                String cleanJson = cleanJsonSnippet(aiRes.get());
                List<QuizQuestionDTO> quiz = objectMapper.readValue(cleanJson, new TypeReference<List<QuizQuestionDTO>>() {});
                if (quiz != null && !quiz.isEmpty()) {
                    return quiz;
                }
            } catch (Exception e) {
                System.err.println("Failed to parse quiz JSON: " + e.getMessage());
            }
        }

        // Fallback: rule-based quiz generator
        List<QuizQuestionDTO> questions = new ArrayList<>();
        List<String> keywords = extractKeywords(text);
        
        if (keywords.size() >= 2) {
            String kw1 = keywords.get(0);
            String kw2 = keywords.get(1);
            
            questions.add(new QuizQuestionDTO(
                    "What is the primary role of " + kw1 + " as referenced in the note?",
                    List.of("It serves as a core configuration standard", "It is utilized for testing parameters", "It functions as the database index", "It is undefined"),
                    "It serves as a core configuration standard"
            ));

            questions.add(new QuizQuestionDTO(
                    "How does " + kw2 + " relate to the main contents of \"" + note.getTitle() + "\"?",
                    List.of("It provides secondary context details", "It acts as a primary controller element", "It handles security validations", "It is deleted during runtime"),
                    "It provides secondary context details"
            ));
        } else {
            questions.add(new QuizQuestionDTO(
                    "What is the subject of the note titled \"" + note.getTitle() + "\"?",
                    List.of(note.getTitle() + " topics", "General software engineering", "Personal finance planning", "Historical literature"),
                    note.getTitle() + " topics"
            ));
            questions.add(new QuizQuestionDTO(
                    "Which of the following is a tag associated with \"" + note.getTitle() + "\"",
                    List.of("Learning", "Random", "Notely Core", "None of the above"),
                    "Learning"
            ));
        }
        return questions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> suggestTags(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.READ);
        String text = note.getContent() + " " + note.getTitle();

        String systemPrompt = "You are a metadata assistant. Suggest exactly 3-4 single-word tags (no '#' prefix, lowercase, alphanumeric) relevant to the note. Return them as a JSON array of strings. Return ONLY valid raw JSON, no markdown, no backticks.";
        Optional<String> aiRes = callSpringAi(systemPrompt, text);
        if (aiRes.isPresent()) {
            try {
                String cleanJson = cleanJsonSnippet(aiRes.get());
                List<String> tags = objectMapper.readValue(cleanJson, new TypeReference<List<String>>() {});
                if (tags != null && !tags.isEmpty()) {
                    return tags;
                }
            } catch (Exception e) {
                System.err.println("Failed to parse tags JSON: " + e.getMessage());
            }
        }

        // Fallback: rule-based tag extractor
        List<String> suggested = new ArrayList<>(extractKeywords(text));
        if (suggested.isEmpty()) {
            suggested.addAll(List.of("notes", "general", "inbox"));
        }
        return suggested.stream().distinct().limit(4).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String improveFormatting(UUID noteId) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.READ);
        String text = note.getContent();
        if (text == null || text.trim().isEmpty()) {
            return "## " + note.getTitle() + "\n\n*Add notes content here...*";
        }

        String systemPrompt = "You are Notely AI, an elite layout and typography editor. Restructure, clean up, and polish the formatting of the user's markdown note. Standardize markdown heading hierarchies, improve spacing, format lists cleanly, bold key definitions, and structure code blocks with language indicators. Add a clean, brief Table of Contents at the beginning if the note is long. Return ONLY the polished markdown note content without any conversational preambles or postscripts.";
        Optional<String> aiRes = callSpringAi(systemPrompt, text);
        if (aiRes.isPresent()) {
            return aiRes.get();
        }

        // Fallback: rule-based formatter
        String formatted = text
                .replaceAll("(?m)^([A-Za-z0-9 ]+)\\n={3,}", "# $1")
                .replaceAll("(?m)^([A-Za-z0-9 ]+)\\n-{3,}", "## $1")
                .replaceAll("(?m)^\\*\\s+", "• ")
                .replaceAll("\\n{3,}", "\n\n");
        
        if (!formatted.startsWith("# ")) {
            formatted = "# " + note.getTitle() + "\n\n" + formatted;
        }
        return formatted;
    }

    private List<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        List<String> keywords = new ArrayList<>();
        String[] words = text.toLowerCase().replaceAll("[^a-zA-Z\\s]", "").split("\\s+");
        Set<String> stopwords = Set.of(
            "the", "a", "an", "and", "or", "but", "if", "then", "else", "of", "to", "in", "on", 
            "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", 
            "before", "after", "above", "below", "from", "up", "down", "is", "are", "was", "were", 
            "be", "been", "being", "have", "has", "had", "do", "does", "did", "this", "that"
        );

        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            if (w.length() > 3 && !stopwords.contains(w)) {
                freq.put(w, freq.getOrDefault(w, 0) + 1);
            }
        }

        freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> keywords.add(e.getKey()));

        return keywords;
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

    @Override
    @Transactional(readOnly = true)
    public String chat(UUID noteId, String userMessage, List<Map<String, String>> history) {
        Note note = getAndVerifyNoteAccess(noteId, SharePermission.READ);
        String noteContent = note.getContent();

        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("You are Notely AI, a sophisticated, contextual AI writing and research assistant built into the Notely Smart Notes App.\n")
                    .append("You have active visibility over the user's current note.\n")
                    .append("Details of the active note:\n")
                    .append("- Title: \"").append(note.getTitle()).append("\"\n")
                    .append("- Notebook: ").append(note.getNotebook() != null ? note.getNotebook().getName() : "None").append("\n")
                    .append("- Folder: ").append(note.getFolder() != null ? note.getFolder().getName() : "None").append("\n\n")
                    .append("Here is the current content of the note (formatted in Markdown):\n")
                    .append("=================== START NOTE CONTENT ===================\n")
                    .append(noteContent == null || noteContent.trim().isEmpty() ? "[Empty Note Content]" : noteContent).append("\n")
                    .append("=================== END NOTE CONTENT ===================\n\n")
                    .append("Instruction: Help the user analyze, write, rephrase, format, draft, or answer questions regarding this note. ")
                    .append("Provide rich markdown formatting (bolding, sections, code snippets, lists, tables) in your response where appropriate. ")
                    .append("Keep answers engaging, smart, and direct. Avoid meta-commentary like 'Based on the note content provided...'. Just answer directly and naturally.");

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt.toString()));

        if (history != null) {
            for (Map<String, String> msg : history) {
                String role = msg.get("role");
                String content = msg.get("content");
                if ("user".equalsIgnoreCase(role)) {
                    messages.add(new UserMessage(content));
                } else if ("assistant".equalsIgnoreCase(role) || "ai".equalsIgnoreCase(role)) {
                    messages.add(new AssistantMessage(content));
                }
            }
        }

        messages.add(new UserMessage(userMessage));

        try {
            Prompt prompt = new Prompt(messages);
            ChatResponse chatResponse = chatClient.call(prompt);
            if (chatResponse != null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                String responseText = chatResponse.getResult().getOutput().getContent();
                if (responseText != null && !responseText.trim().isEmpty()) {
                    return responseText.trim();
                }
            }
        } catch (Exception e) {
            System.err.println("Spring AI Chat completion failed, falling back: " + e.getMessage());
        }

        // Fallback: rule-based helper based on query matching
        String queryLower = userMessage.toLowerCase();
        if (queryLower.contains("summary") || queryLower.contains("summarize")) {
            return "### Notely AI Summary Fallback\nThis note is titled \"" + note.getTitle() + "\". It has " + (noteContent != null ? noteContent.length() : 0) + " characters. Key sentences discuss core concepts related to " + note.getTitle() + ". Please configure your `OPENAI_API_KEY` to unlock full conversational GPT answers.";
        }
        if (queryLower.contains("action items") || queryLower.contains("todo") || queryLower.contains("tasks")) {
            return "### Suggested Action Items (Fallback)\n- [ ] Review note titled \"" + note.getTitle() + "\"\n- [ ] Supplement documentation with tags\n- [ ] Invite collaborators to share workspace\n\n*Configure a valid OpenAI API Key to generate real action items.*";
        }
        return "👋 Hi there! I am Notely AI. I see you are working on the note **" + note.getTitle() + "**. I'm currently running in Local Fallback mode because the Spring AI model could not be reached (missing or invalid API key). Ask me to 'summarize' or list 'action items' to see specific text generations, or configure a valid `OPENAI_API_KEY` to enable full Conversational GPT power!";
    }
}
