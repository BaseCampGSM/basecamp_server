package com.example.basecamp_server.domain.report.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ClaudeResponseDto {
    private List<Content> content;

    @Getter
    @NoArgsConstructor
    public static class Content {
        private String text;
    }
}