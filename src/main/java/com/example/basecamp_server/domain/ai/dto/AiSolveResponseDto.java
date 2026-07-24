package com.example.basecamp_server.domain.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AiSolveResponseDto {
    private String status;
    private String category;
    private String urgency;
    private String solution;
    private List<String> sources;
}