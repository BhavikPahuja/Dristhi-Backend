package com.project.tekathon.drishti.service;

import com.project.tekathon.drishti.dto.NetworkDtos.CytoscapeGraphResponse;
import com.project.tekathon.drishti.dto.NetworkDtos.NetworkEdge;
import com.project.tekathon.drishti.dto.NetworkDtos.NetworkNode;
import com.project.tekathon.drishti.dto.NetworkDtos.NetworkPathResponse;
import com.project.tekathon.drishti.dto.NetworkDtos.NetworkPathSegment;
import com.project.tekathon.drishti.dto.NetworkDtos.NetworkResponse;
import com.project.tekathon.drishti.dto.NetworkDtos.RelationshipDetailResponse;
import com.project.tekathon.drishti.entity.AccountEntity;
import com.project.tekathon.drishti.entity.CaseEntity;
import com.project.tekathon.drishti.entity.DocumentEntity;
import com.project.tekathon.drishti.entity.LocationEntity;
import com.project.tekathon.drishti.entity.PersonEntity;
import com.project.tekathon.drishti.entity.PhoneEntity;
import com.project.tekathon.drishti.entity.RelationshipEntity;
import com.project.tekathon.drishti.entity.VehicleEntity;
import com.project.tekathon.drishti.exception.ResourceNotFoundException;
import com.project.tekathon.drishti.repository.AccountRepository;
import com.project.tekathon.drishti.repository.CaseRepository;
import com.project.tekathon.drishti.repository.DocumentRepository;
import com.project.tekathon.drishti.repository.EvidenceRepository;
import com.project.tekathon.drishti.repository.LocationRepository;
import com.project.tekathon.drishti.repository.PersonRepository;
import com.project.tekathon.drishti.repository.PhoneRepository;
import com.project.tekathon.drishti.repository.RelationshipRepository;
import com.project.tekathon.drishti.repository.VehicleRepository;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkService {

    private final RelationshipRepository relationshipRepository;
    private final EvidenceRepository evidenceRepository;
    private final PersonRepository personRepository;
    private final PhoneRepository phoneRepository;
    private final VehicleRepository vehicleRepository;
    private final AccountRepository accountRepository;
    private final LocationRepository locationRepository;
    private final CaseRepository caseRepository;
    private final DocumentRepository documentRepository;
    private final Neo4jClient neo4jClient;

    public void syncCase(CaseEntity entity) {
        syncNode(entity.getCaseId(), "CASE", entity.getTitle());
    }

    public void syncPerson(PersonEntity entity) {
        syncNode(entity.getPersonId(), "PERSON", entity.getName());
    }

    public void syncPhone(PhoneEntity entity) {
        syncNode(entity.getPhoneId(), "PHONE", entity.getPhoneNumber());
        if (entity.getOwnerPersonId() != null) {
            syncRelationship(entity.getOwnerPersonId(), entity.getPhoneId(), "USED", 0.99, null, null, null, null);
        }
    }

    public void syncVehicle(VehicleEntity entity) {
        syncNode(entity.getVehicleId(), "VEHICLE", entity.getRegistrationNumber());
        if (entity.getOwnerPersonId() != null) {
            syncRelationship(entity.getOwnerPersonId(), entity.getVehicleId(), "OWNS", 0.99, null, null, null, null);
        }
    }

    public void syncAccount(AccountEntity entity) {
        syncNode(entity.getAccountId(), "ACCOUNT", entity.getAccountNumber());
        if (entity.getOwnerPersonId() != null) {
            syncRelationship(entity.getOwnerPersonId(), entity.getAccountId(), "OWNS", 0.99, null, null, null, null);
        }
    }

    public void syncLocation(LocationEntity entity) {
        syncNode(entity.getLocationId(), "LOCATION", entity.getName());
    }

    public void syncDocument(DocumentEntity entity) {
        syncNode(entity.getDocumentId(), "DOCUMENT", entity.getTitle());
        if (entity.getCaseId() != null) {
            syncRelationship(entity.getCaseId(), entity.getDocumentId(), "PART_OF", 0.99, null, null, null, null);
        }
    }

    public void syncRelationship(RelationshipEntity entity) {
        syncRelationship(entity.getSourceId(), entity.getTargetId(), entity.getRelationship(), entity.getConfidence(),
                entity.getRelationshipId(), entity.getEvidenceIdsJson(), entity.getSourceDocumentIdsJson(), entity.getSupportText());
    }

    public NetworkResponse getEntityNetwork(String entityId, int depth) {
        return buildNetwork(entityId, depth);
    }

    public NetworkResponse getPersonNetwork(String personId, int depth) {
        if (personRepository.findById(personId).isEmpty()) {
            throw new ResourceNotFoundException("Person " + personId + " not found");
        }
        return buildNetwork(personId, depth);
    }

    public CytoscapeGraphResponse getCytoscapeNetwork(String entityId, int depth) {
        NetworkResponse network = buildNetwork(entityId, depth);
        List<Map<String, Object>> nodes = network.nodes().stream()
                .map(node -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("data", Map.of("id", node.id(), "label", node.label(), "type", node.type()));
                    return data;
                })
                .collect(Collectors.toList());
        List<Map<String, Object>> edges = network.edges().stream()
                .map(edge -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("data", Map.of(
                            "id", edge.id(),
                            "source", edge.source(),
                            "target", edge.target(),
                            "label", edge.relationship(),
                            "confidence", edge.confidence()));
                    return data;
                })
                .collect(Collectors.toList());
        return new CytoscapeGraphResponse(nodes, edges);
    }

    public NetworkPathResponse findPath(String sourceId, String targetId, int maxDepth) {
        Map<String, Set<String>> adjacency = buildAdjacency();
        Queue<List<String>> queue = new ArrayDeque<>();
        queue.add(List.of(sourceId));
        Set<String> visitedPaths = new HashSet<>();

        List<NetworkPathSegment> paths = new ArrayList<>();
        while (!queue.isEmpty()) {
            List<String> path = queue.poll();
            String current = path.get(path.size() - 1);
            if (path.size() - 1 > maxDepth) {
                continue;
            }
            if (current.equals(targetId) && path.size() > 1) {
                paths.add(new NetworkPathSegment(path, relationshipsForPath(path), path.size() - 1));
                continue;
            }
            for (String next : adjacency.getOrDefault(current, Collections.emptySet())) {
                if (path.contains(next)) {
                    continue;
                }
                List<String> newPath = new ArrayList<>(path);
                newPath.add(next);
                String key = String.join(">", newPath);
                if (visitedPaths.add(key)) {
                    queue.add(newPath);
                }
            }
        }
        return new NetworkPathResponse(sourceId, targetId, !paths.isEmpty(), paths);
    }

    public List<RelationshipDetailResponse> relationshipDetails(String relationshipId) {
        RelationshipEntity relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship " + relationshipId + " not found"));
        return List.of(toRelationshipDetail(relationship));
    }

    public List<Map<String, Object>> communities() {
        Map<String, Set<String>> adjacency = buildAdjacency();
        Set<String> visited = new HashSet<>();
        List<Map<String, Object>> communities = new ArrayList<>();
        int index = 1;
        for (String node : adjacency.keySet()) {
            if (!visited.add(node)) {
                continue;
            }
            Set<String> component = new HashSet<>();
            Queue<String> queue = new ArrayDeque<>();
            queue.add(node);
            component.add(node);
            while (!queue.isEmpty()) {
                String current = queue.poll();
                for (String next : adjacency.getOrDefault(current, Collections.emptySet())) {
                    if (visited.add(next)) {
                        component.add(next);
                        queue.add(next);
                    }
                }
            }
            communities.add(Map.of("communityId", "COMM" + String.format(Locale.ROOT, "%03d", index++), "memberIds", component));
        }
        return communities;
    }

    private NetworkResponse buildNetwork(String entityId, int depth) {
        Map<String, NodeInfo> nodes = new LinkedHashMap<>();
        List<NetworkEdge> edges = new ArrayList<>();
        Queue<PathNode> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(new PathNode(entityId, 0));
        visited.add(entityId);

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            NodeInfo currentInfo = lookup(current.entityId);
            nodes.putIfAbsent(current.entityId, currentInfo);
            if (current.depth >= depth) {
                continue;
            }
            for (RelationshipEntity relationship : adjacentRelationships(current.entityId)) {
                String neighbor = relationship.getSourceId().equals(current.entityId)
                        ? relationship.getTargetId()
                        : relationship.getSourceId();
                NodeInfo neighborInfo = lookup(neighbor);
                nodes.putIfAbsent(neighbor, neighborInfo);
                edges.add(new NetworkEdge(
                        relationship.getRelationshipId(),
                        relationship.getSourceId(),
                        relationship.getTargetId(),
                        relationship.getRelationship(),
                        relationship.getConfidence()));
                if (visited.add(neighbor)) {
                    queue.add(new PathNode(neighbor, current.depth + 1));
                }
            }
        }

        return new NetworkResponse(entityId,
                nodes.values().stream().map(info -> new NetworkNode(info.id(), info.type(), info.label())).toList(),
                dedupeEdges(edges));
    }

    private List<NetworkEdge> dedupeEdges(List<NetworkEdge> edges) {
        Map<String, NetworkEdge> unique = new LinkedHashMap<>();
        for (NetworkEdge edge : edges) {
            unique.putIfAbsent(edge.id(), edge);
        }
        return new ArrayList<>(unique.values());
    }

    private List<RelationshipEntity> adjacentRelationships(String entityId) {
        return relationshipRepository.findAll().stream()
                .filter(relationship -> entityId.equals(relationship.getSourceId()) || entityId.equals(relationship.getTargetId()))
                .sorted(Comparator.comparing(RelationshipEntity::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    private Map<String, Set<String>> buildAdjacency() {
        Map<String, Set<String>> adjacency = new HashMap<>();
        for (RelationshipEntity relationship : relationshipRepository.findAll()) {
            adjacency.computeIfAbsent(relationship.getSourceId(), key -> new HashSet<>()).add(relationship.getTargetId());
            adjacency.computeIfAbsent(relationship.getTargetId(), key -> new HashSet<>()).add(relationship.getSourceId());
        }
        return adjacency;
    }

    private List<String> relationshipsForPath(List<String> path) {
        List<String> relationshipTypes = new ArrayList<>();
        for (int index = 0; index < path.size() - 1; index++) {
            String source = path.get(index);
            String target = path.get(index + 1);
            String relationship = relationshipRepository.findAll().stream()
                    .filter(candidate -> (candidate.getSourceId().equals(source) && candidate.getTargetId().equals(target))
                            || (candidate.getSourceId().equals(target) && candidate.getTargetId().equals(source)))
                    .map(RelationshipEntity::getRelationship)
                    .findFirst()
                    .orElse("CONNECTED_TO");
            relationshipTypes.add(relationship);
        }
        return relationshipTypes;
    }

    private RelationshipDetailResponse toRelationshipDetail(RelationshipEntity relationship) {
        return new RelationshipDetailResponse(
                relationship.getRelationshipId(),
                relationship.getSourceId(),
                relationship.getTargetId(),
                relationship.getRelationship(),
                relationship.getConfidence(),
                relationship.getFirstObserved() == null ? null : relationship.getFirstObserved().toString(),
                relationship.getLastObserved() == null ? null : relationship.getLastObserved().toString(),
                splitJsonList(relationship.getEvidenceIdsJson()),
                splitJsonList(relationship.getSourceDocumentIdsJson()));
    }

    private List<String> splitJsonList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        String normalized = json.replace("[", "").replace("]", "").replace("\"", "");
        if (normalized.isBlank()) {
            return List.of();
        }
        return List.of(normalized.split(",\\s*"));
    }

    private void syncNode(String id, String type, String label) {
        try {
            var querySpec = neo4jClient.query("""
                    MERGE (n:ENTITY {id: $id})
                    SET n.type = $type,
                        n.label = $label
                    """);
            if (querySpec != null) {
                querySpec
                        .bind(id).to("id")
                        .bind(type).to("type")
                        .bind(label).to("label")
                        .run();
            }
        } catch (Exception ex) {
            log.warn("Failed to sync graph node {}: {}", id, ex.getMessage());
        }
    }

    private void syncRelationship(String sourceId, String targetId, String relationship, Double confidence,
            String relationshipId, String evidenceIdsJson, String sourceDocumentIdsJson, String supportText) {
        try {
            var querySpec = neo4jClient.query("""
                    MATCH (source:ENTITY {id: $sourceId})
                    MATCH (target:ENTITY {id: $targetId})
                    MERGE (source)-[r:CONNECTED_TO {relationshipId: coalesce($relationshipId, randomUUID())}]->(target)
                    SET r.relationship = $relationship,
                        r.confidence = $confidence,
                        r.evidenceIdsJson = $evidenceIdsJson,
                        r.sourceDocumentIdsJson = $sourceDocumentIdsJson,
                        r.supportText = $supportText
                    """);
            if (querySpec != null) {
                querySpec
                        .bind(sourceId).to("sourceId")
                        .bind(targetId).to("targetId")
                        .bind(relationship).to("relationship")
                        .bind(confidence).to("confidence")
                        .bind(relationshipId).to("relationshipId")
                        .bind(evidenceIdsJson).to("evidenceIdsJson")
                        .bind(sourceDocumentIdsJson).to("sourceDocumentIdsJson")
                        .bind(supportText).to("supportText")
                        .run();
            }
        } catch (Exception ex) {
            log.warn("Failed to sync graph relationship {} -> {}: {}", sourceId, targetId, ex.getMessage());
        }
    }

    private NodeInfo lookup(String entityId) {
        return switch (entityId == null ? "" : entityId.substring(0, Math.min(entityId.length(), 3)).toUpperCase(Locale.ROOT)) {
            case "PH0", "PH" -> phoneRepository.findById(entityId)
                    .map(phone -> new NodeInfo(phone.getPhoneId(), "PHONE", phone.getPhoneNumber()))
                    .orElse(new NodeInfo(entityId, "PHONE", entityId));
            case "V00", "V" -> vehicleRepository.findById(entityId)
                    .map(vehicle -> new NodeInfo(vehicle.getVehicleId(), "VEHICLE", vehicle.getRegistrationNumber()))
                    .orElse(new NodeInfo(entityId, "VEHICLE", entityId));
            case "ACC" -> accountRepository.findById(entityId)
                    .map(account -> new NodeInfo(account.getAccountId(), "ACCOUNT", account.getAccountNumber()))
                    .orElse(new NodeInfo(entityId, "ACCOUNT", entityId));
            case "LOC" -> locationRepository.findById(entityId)
                    .map(location -> new NodeInfo(location.getLocationId(), "LOCATION", location.getName()))
                    .orElse(new NodeInfo(entityId, "LOCATION", entityId));
            case "CAS", "CAS0", "CASE" -> caseRepository.findById(entityId)
                    .map(caseEntity -> new NodeInfo(caseEntity.getCaseId(), "CASE", caseEntity.getTitle()))
                    .orElse(new NodeInfo(entityId, "CASE", entityId));
            case "DOC" -> documentRepository.findById(entityId)
                    .map(document -> new NodeInfo(document.getDocumentId(), "DOCUMENT", document.getTitle()))
                    .orElse(new NodeInfo(entityId, "DOCUMENT", entityId));
            default -> personRepository.findById(entityId)
                    .map(person -> new NodeInfo(person.getPersonId(), "PERSON", person.getName()))
                    .orElse(new NodeInfo(entityId, "ENTITY", entityId));
        };
    }

    private record PathNode(String entityId, int depth) {
    }

    private record NodeInfo(String id, String type, String label) {
    }
}
