package com.example.basecamp_server.domain.publicdata.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "public_facility")
@Entity
public class PublicFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String facilityId; // 공공데이터 고유 ID

    private String name;        // 시설/정책명
    private String category;    // 카테고리
    private String address;     // 주소
    private String phone;       // 전화번호

    @Builder
    public PublicFacility(String facilityId, String name, String category, String address, String phone) {
        this.facilityId = facilityId;
        this.name = name;
        this.category = category;
        this.address = address;
        this.phone = phone;
    }

    public void updateInfo(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }
}