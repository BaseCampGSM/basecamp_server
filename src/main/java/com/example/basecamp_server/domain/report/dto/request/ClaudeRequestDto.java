package com.example.basecamp_server.domain.report.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ClaudeRequestDto {
    private String model;
    private int max_tokens;
    private List<Message> messages;

    @Getter
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}