package com.example.basecamp_server.domain.place.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceNearbyResponseDto {
    private String name;
    private Double lat;
    private Double lng;
    private String address;
    private String category;
}