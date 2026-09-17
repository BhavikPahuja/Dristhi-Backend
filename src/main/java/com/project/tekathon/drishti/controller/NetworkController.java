package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.NetworkDtos.CytoscapeGraphResponse;
import com.project.tekathon.drishti.dto.NetworkDtos.NetworkPathResponse;
import com.project.tekathon.drishti.dto.NetworkDtos.NetworkResponse;
import com.project.tekathon.drishti.dto.NetworkDtos.RelationshipDetailResponse;
import com.project.tekathon.drishti.service.NetworkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/network")
@RequiredArgsConstructor
public class NetworkController {

    private final NetworkService networkService;

    @GetMapping("/person/{personId}")
    public NetworkResponse personNetwork(@PathVariable String personId, @RequestParam(defaultValue = "2") int depth) {
        return networkService.getPersonNetwork(personId, depth);
    }

    @GetMapping("/entity/{entityId}")
    public CytoscapeGraphResponse entityNetwork(@PathVariable String entityId, @RequestParam(defaultValue = "2") int depth) {
        return networkService.getCytoscapeNetwork(entityId, depth);
    }

    @GetMapping("/path")
    public NetworkPathResponse path(@RequestParam String source, @RequestParam String target, @RequestParam(defaultValue = "5") int maxDepth) {
        return networkService.findPath(source, target, maxDepth);
    }

    @GetMapping("/communities")
    public List<?> communities() {
        return networkService.communities();
    }

    @GetMapping("/relationship/{relationshipId}")
    public List<RelationshipDetailResponse> relationship(@PathVariable String relationshipId) {
        return networkService.relationshipDetails(relationshipId);
    }
}
