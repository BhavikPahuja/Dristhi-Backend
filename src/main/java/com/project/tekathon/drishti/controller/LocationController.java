package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.InvestigationDtos.CreateLocationRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.LocationEventRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.LocationResponse;
import com.project.tekathon.drishti.service.InvestigationDataService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LocationController {

    private final InvestigationDataService investigationDataService;

    @PostMapping("/locations")
    public ResponseEntity<LocationResponse> createLocation(@RequestBody CreateLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investigationDataService.createLocation(request));
    }

    @PostMapping("/location-events")
    public ResponseEntity<?> createLocationEvent(@RequestBody LocationEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                investigationDataService.createEvent(new com.project.tekathon.drishti.dto.InvestigationDtos.CreateEventRequest(
                        "CASE-UNKNOWN", "VISIT", request.timestamp(), request.locationId(), request.source(), null, request.personId())));
    }

    @GetMapping("/locations/{locationId}/activity")
    public List<com.project.tekathon.drishti.dto.InvestigationDtos.SurveillanceResponse> activity(@PathVariable String locationId) {
        return investigationDataService.locationActivity(locationId);
    }

    @GetMapping("/locations/{locationId}/persons")
    public List<String> personsAtLocation(@PathVariable String locationId) {
        return investigationDataService.personsAtLocation(locationId);
    }
}
