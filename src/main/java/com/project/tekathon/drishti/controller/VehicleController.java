package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.InvestigationDtos.VehicleResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateVehicleRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.SurveillanceResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final InvestigationDataService investigationDataService;

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@RequestBody CreateVehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investigationDataService.createVehicle(request));
    }

    @GetMapping
    public List<VehicleResponse> list() {
        return investigationDataService.listVehicles();
    }

    @GetMapping("/{vehicleId}")
    public VehicleResponse get(@PathVariable String vehicleId) {
        return investigationDataService.getVehicle(vehicleId);
    }

    @GetMapping("/{vehicleId}/history")
    public List<Object> history(@PathVariable String vehicleId) {
        return investigationDataService.vehicleHistory(vehicleId);
    }

    @GetMapping("/{vehicleId}/surveillance")
    public List<SurveillanceResponse> surveillance(@PathVariable String vehicleId) {
        return investigationDataService.vehicleSurveillance(vehicleId);
    }

    @GetMapping("/search")
    public List<VehicleResponse> search(@RequestParam("registration") String registration) {
        return investigationDataService.searchVehicle(registration);
    }

    @GetMapping("/person/{personId}")
    public List<VehicleResponse> personVehicles(@PathVariable String personId) {
        return investigationDataService.personVehicles(personId);
    }
}
