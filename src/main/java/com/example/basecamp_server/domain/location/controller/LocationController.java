package com.example.basecamp_server.domain.location.controller;

import com.example.basecamp_server.domain.location.dto.KakaoAddressResponseDto;
import com.example.basecamp_server.domain.location.dto.KakaoKeywordResponseDto;
import com.example.basecamp_server.domain.location.service.KakaoLocalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/location")
@RestController
public class LocationController {

    private final KakaoLocalService kakaoLocalService;

    /**
     * 주소 ➔ 위도/경도 변환 API
     * 예시: GET /api/v1/location/address?query=서울특별시 중구 세종대로 110
     */
    @GetMapping("/address")
    public ResponseEntity<KakaoAddressResponseDto> getCoordinatesByAddress(@RequestParam String query) {
        KakaoAddressResponseDto result = kakaoLocalService.searchAddress(query);
        return ResponseEntity.ok(result);
    }

    /**
     * 키워드로 장소 검색 API
     * 예시: GET /api/v1/location/keyword?query=강남역 맛집
     */
    @GetMapping("/keyword")
    public ResponseEntity<KakaoKeywordResponseDto> searchByKeyword(@RequestParam String query) {
        KakaoKeywordResponseDto result = kakaoLocalService.searchKeyword(query);
        return ResponseEntity.ok(result);
    }
}