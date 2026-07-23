package com.example.basecamp_server.domain.place.controller;

import com.example.basecamp_server.domain.location.dto.KakaoKeywordResponseDto;
import com.example.basecamp_server.domain.location.service.KakaoLocalService;
import com.example.basecamp_server.domain.place.dto.PlaceNearbyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class PlaceController {

    private final KakaoLocalService kakaoLocalService;

    /** 3-4. 주변 장소 조회 GET /api/v1/places/nearby?lat=&lng=&category= */
    @GetMapping("/places/nearby")
    public ResponseEntity<List<PlaceNearbyResponseDto>> getNearbyPlaces(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam String category) {

        KakaoKeywordResponseDto kakaoResult = kakaoLocalService.searchKeyword(category);

        List<PlaceNearbyResponseDto> result = kakaoResult.getDocuments().stream()
                .map(doc -> PlaceNearbyResponseDto.builder()
                        .name(doc.getPlaceName())
                        .lat(Double.parseDouble(doc.getY()))
                        .lng(Double.parseDouble(doc.getX()))
                        .address(doc.getRoadAddressName() != null && !doc.getRoadAddressName().isEmpty() ? doc.getRoadAddressName() : doc.getAddressName())
                        .category(category)
                        .build())
                .toList();

        return ResponseEntity.ok(result);
    }
}