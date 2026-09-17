package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.service.EntityLookupService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entities")
@RequiredArgsConstructor
public class EntityController {

    private final EntityLookupService entityLookupService;

    @GetMapping("/{entityId}")
    public Map<String, Object> get(@PathVariable String entityId) {
        return entityLookupService.get(entityId);
    }

    @GetMapping("/{entityId}/connections")
    public Object connections(@PathVariable String entityId, @RequestParam(defaultValue = "2") int depth) {
        return entityLookupService.connections(entityId, depth);
    }

    @GetMapping("/{entityId}/search")
    public List<Map<String, Object>> search(@PathVariable String entityId, @RequestParam String query) {
        return entityLookupService.search(query);
    }
}
