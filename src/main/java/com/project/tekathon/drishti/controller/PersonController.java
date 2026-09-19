package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.InvestigationDtos.LocationResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.SurveillanceResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.TransactionResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.VehicleResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.CdrResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonConnectionResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonProfileResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonResponse;
import com.project.tekathon.drishti.dto.PersonDtos.PersonSearchResponse;
import com.project.tekathon.drishti.entity.TransactionEntity;
import com.project.tekathon.drishti.service.PersonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping
    public List<PersonResponse> listAll() {
        return personService.listAll();
    }

    @GetMapping("/search")
    public List<PersonSearchResponse> search(@RequestParam String query) {
        return personService.search(query);
    }

    @GetMapping("/{personId}")
    public PersonResponse get(@PathVariable String personId) {
        return personService.get(personId);
    }

    @GetMapping("/{personId}/profile")
    public PersonProfileResponse profile(@PathVariable String personId) {
        return personService.profile(personId);
    }

    @GetMapping("/{personId}/connections")
    public List<PersonConnectionResponse> connections(@PathVariable String personId) {
        return personService.connections(personId);
    }

    @GetMapping("/{personId}/calls")
    public List<CdrResponse> calls(@PathVariable String personId) {
        return personService.calls(personId).stream()
                .map(cdr -> new CdrResponse(cdr.getCdrId(), cdr.getCallerPhoneId(), cdr.getReceiverPhoneId(), cdr.getTimestamp(), cdr.getDurationSeconds(), cdr.getCaseId()))
                .toList();
    }

    @GetMapping("/{personId}/transactions")
    public List<TransactionResponse> transactions(@PathVariable String personId) {
        return personService.transactions(personId).stream()
                .map(tx -> new TransactionResponse(tx.getTransactionId(), tx.getSourceAccountId(), tx.getDestinationAccountId(), tx.getAmount(), tx.getCurrency(), tx.getTimestamp(), tx.getDescription(), tx.getCaseId()))
                .toList();
    }

    @GetMapping("/{personId}/vehicles")
    public List<VehicleResponse> vehicles(@PathVariable String personId) {
        return personService.vehicles(personId).stream()
                .map(vehicle -> new VehicleResponse(vehicle.getVehicleId(), vehicle.getRegistrationNumber(), vehicle.getMake(), vehicle.getModel(), vehicle.getOwnerPersonId()))
                .toList();
    }

    @GetMapping("/{personId}/locations")
    public List<LocationResponse> locations(@PathVariable String personId) {
        return personService.locations(personId);
    }

    @GetMapping("/{personId}/surveillance")
    public List<SurveillanceResponse> surveillance(@PathVariable String personId) {
        return personService.surveillance(personId);
    }
}
