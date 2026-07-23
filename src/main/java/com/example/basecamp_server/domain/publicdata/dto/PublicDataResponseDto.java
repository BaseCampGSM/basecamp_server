package com.example.basecamp_server.domain.publicdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PublicDataResponseDto {

    private Response response;

    @Getter
    @NoArgsConstructor
    public static class Response {
        private Body body;
    }

    @Getter
    @NoArgsConstructor
    public static class Body {
        private Items items;
    }

    @Getter
    @NoArgsConstructor
    public static class Items {
        private List<Item> item;
    }

    @Getter
    @NoArgsConstructor
    public static class Item {
        @JsonProperty("facilityId")
        private String facilityId;

        @JsonProperty("facilityName")
        private String facilityName;

        @JsonProperty("category")
        private String category;

        @JsonProperty("address")
        private String address;

        @JsonProperty("phone")
        private String phone;
    }
}