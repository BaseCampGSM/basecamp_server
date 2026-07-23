package com.example.basecamp_server.domain.publicdata.repository;

import com.example.basecamp_server.domain.publicdata.entity.PublicFacility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicFacilityRepository extends JpaRepository<PublicFacility, Long> {
    Optional<PublicFacility> findByFacilityId(String facilityId);
}