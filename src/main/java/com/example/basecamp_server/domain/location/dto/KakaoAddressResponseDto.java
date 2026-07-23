package com.example.basecamp_server.domain.location.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoAddressResponseDto {

    private List<Document> documents;

    @Getter
    @NoArgsConstructor
    public static class Document {
        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("x") // 경도 (Longitude)
        private String x;

        @JsonProperty("y") // 위도 (Latitude)
        private String y;
    }
}
