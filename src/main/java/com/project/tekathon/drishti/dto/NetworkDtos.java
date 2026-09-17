package com.project.tekathon.drishti.dto;

import java.util.List;

public final class NetworkDtos {

    private NetworkDtos() {
    }

    public record NetworkNode(String id, String type, String label) {
    }

    public record NetworkEdge(String id, String source, String target, String relationship, Double confidence) {
    }

    public record CytoscapeNodeData(String id, String label, String type) {
    }

    public record CytoscapeNode(String data) {
    }

    public record CytoscapeEdgeData(String id, String source, String target, String label, Double confidence) {
    }

    public record CytoscapeEdge(String data) {
    }

    public record NetworkResponse(String rootEntity, List<NetworkNode> nodes, List<NetworkEdge> edges) {
    }

    public record CytoscapeGraphResponse(List<java.util.Map<String, Object>> nodes, List<java.util.Map<String, Object>> edges) {
    }

    public record NetworkPathSegment(List<String> nodes, List<String> relationships, Integer length) {
    }

    public record NetworkPathResponse(String source, String target, Boolean pathFound, List<NetworkPathSegment> paths) {
    }

    public record CommunityResponse(String communityId, List<String> memberIds) {
    }

    public record RelationshipDetailResponse(
            String relationshipId,
            String sourceId,
            String targetId,
            String relationship,
            Double confidence,
            String firstObserved,
            String lastObserved,
            List<String> evidenceIds,
            List<String> sourceDocumentIds) {
    }
}
