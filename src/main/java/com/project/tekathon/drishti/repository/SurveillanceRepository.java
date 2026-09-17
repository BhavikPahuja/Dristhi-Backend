package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.SurveillanceReportEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveillanceRepository extends JpaRepository<SurveillanceReportEntity, String> {

    List<SurveillanceReportEntity> findByCaseIdOrderByTimestampDesc(String caseId);

    List<SurveillanceReportEntity> findByPersonIdOrderByTimestampDesc(String personId);

    List<SurveillanceReportEntity> findByVehicleIdOrderByTimestampDesc(String vehicleId);
}
