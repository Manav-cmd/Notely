package com.notely.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AIChatRequest {
    private String message;
    private List<Map<String, String>> history;
}
