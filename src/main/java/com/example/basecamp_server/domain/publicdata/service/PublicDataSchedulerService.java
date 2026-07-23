package com.example.basecamp_server.domain.publicdata.service;

import com.example.basecamp_server.domain.publicdata.dto.PublicDataResponseDto;
import com.example.basecamp_server.domain.publicdata.entity.PublicFacility;
import com.example.basecamp_server.domain.publicdata.repository.PublicFacilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@Service
public class PublicDataSchedulerService {

    private final RestTemplate restTemplate;
    private final PublicFacilityRepository publicFacilityRepository;

    @Value("${api.public-data.key}")
    private String publicDataApiKey;

    /**
     * 매일 새벽 03:00에 공공데이터 수집 스케줄러 실행
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void fetchAndSavePublicData() {
        log.info("=== 공공데이터 정기 수집 스케줄러 시작 ===");

        // 공공데이터포털 API Endpoint URL 예시
        URI uri = UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr/1360000/PublicFacilityInfoService/getFacilityList")
                .queryParam("serviceKey", publicDataApiKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 100)
                .queryParam("dataType", "JSON")
                .build(true) // 인코딩된 serviceKey 유지
                .toUri();

        try {
            PublicDataResponseDto response = restTemplate.getForObject(uri, PublicDataResponseDto.class);

            if (response != null && response.getResponse() != null
                    && response.getResponse().getBody() != null
                    && response.getResponse().getBody().getItems() != null) {

                for (PublicDataResponseDto.Item item : response.getResponse().getBody().getItems().getItem()) {
                    // 이미 존재하는 데이터면 Update, 없으면 Save (Upsert)
                    publicFacilityRepository.findByFacilityId(item.getFacilityId())
                            .ifPresentOrElse(
                                    facility -> facility.updateInfo(item.getFacilityName(), item.getAddress(), item.getPhone()),
                                    () -> publicFacilityRepository.save(PublicFacility.builder()
                                            .facilityId(item.getFacilityId())
                                            .name(item.getFacilityName())
                                            .category(item.getCategory())
                                            .address(item.getAddress())
                                            .phone(item.getPhone())
                                            .build())
                            );
                }
                log.info("=== 공공데이터 수집 완료 ===");
            }
        } catch (Exception e) {
            log.error("공공데이터 수집 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}