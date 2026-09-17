package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.PersonDtos.PersonTimelineItemResponse;
import com.project.tekathon.drishti.service.CaseService;
import com.project.tekathon.drishti.service.EntityLookupService;
import com.project.tekathon.drishti.service.PersonService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TimelineController {

    private final PersonService personService;
    private final CaseService caseService;
    private final EntityLookupService entityLookupService;

    @GetMapping("/persons/{personId}/timeline")
    public List<PersonTimelineItemResponse> personTimeline(@PathVariable String personId) {
        return personService.timeline(personId);
    }

    @GetMapping("/cases/{caseId}/timeline")
    public List<PersonTimelineItemResponse> caseTimeline(@PathVariable String caseId) {
        return caseService.timeline(caseId);
    }

    @GetMapping("/entities/{entityId}/timeline")
    public List<PersonTimelineItemResponse> entityTimeline(@PathVariable String entityId) {
        return entityLookupService.timeline(entityId);
    }
}
