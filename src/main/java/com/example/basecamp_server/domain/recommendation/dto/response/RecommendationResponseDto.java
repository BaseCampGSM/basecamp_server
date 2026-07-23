package com.example.basecamp_server.domain.recommendation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponseDto {
    private String name;
    private String category;
    private Double lat;
    private Double lng;
    private String address;

    @JsonProperty("source_url")
    private String sourceUrl;
}