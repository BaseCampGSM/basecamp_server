package com.example.basecamp_server.domain.location.service;

import com.example.basecamp_server.domain.location.dto.KakaoAddressResponseDto;
import com.example.basecamp_server.domain.location.dto.KakaoKeywordResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@Service
public class KakaoLocalService {

    private final RestTemplate restTemplate;

    @Value("${api.kakao.key}")
    private String kakaoRestApiKey;

    /**
     * 1. 주소를 좌표(위도/경도)로 변환
     */
    public KakaoAddressResponseDto searchAddress(String address) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", address)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<KakaoAddressResponseDto> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                requestEntity,
                KakaoAddressResponseDto.class
        );

        return response.getBody();
    }

    /**
     * 2. 키워드로 장소 검색
     */
    public KakaoKeywordResponseDto searchKeyword(String keyword) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", keyword)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<KakaoKeywordResponseDto> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                requestEntity,
                KakaoKeywordResponseDto.class
        );

        return response.getBody();
    }
}