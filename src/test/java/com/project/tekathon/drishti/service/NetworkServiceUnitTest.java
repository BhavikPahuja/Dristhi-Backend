package com.project.tekathon.drishti.service;

import static org.mockito.Mockito.when;

import com.project.tekathon.drishti.dto.NetworkDtos.NetworkPathResponse;
import com.project.tekathon.drishti.dto.NetworkDtos.NetworkPathSegment;
import com.project.tekathon.drishti.dto.NetworkDtos.NetworkResponse;
import com.project.tekathon.drishti.entity.PersonEntity;
import com.project.tekathon.drishti.entity.RelationshipEntity;
import com.project.tekathon.drishti.repository.AccountRepository;
import com.project.tekathon.drishti.repository.CaseRepository;
import com.project.tekathon.drishti.repository.DocumentRepository;
import com.project.tekathon.drishti.repository.EvidenceRepository;
import com.project.tekathon.drishti.repository.LocationRepository;
import com.project.tekathon.drishti.repository.PersonRepository;
import com.project.tekathon.drishti.repository.PhoneRepository;
import com.project.tekathon.drishti.repository.RelationshipRepository;
import com.project.tekathon.drishti.repository.VehicleRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.neo4j.core.Neo4jClient;

class NetworkServiceUnitTest {

    @Mock
    private RelationshipRepository relationshipRepository;
    @Mock
    private EvidenceRepository evidenceRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private PhoneRepository phoneRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private CaseRepository caseRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private Neo4jClient neo4jClient;

    private NetworkService networkService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        networkService = new NetworkService(relationshipRepository, evidenceRepository, personRepository,
                phoneRepository, vehicleRepository, accountRepository, locationRepository,
                caseRepository, documentRepository, neo4jClient);
    }

    private RelationshipEntity relationship(String id, String source, String target, String type, Instant observed) {
        return RelationshipEntity.builder()
                .relationshipId(id)
                .sourceId(source)
                .targetId(target)
                .relationship(type)
                .confidence(0.9)
                .firstObserved(observed)
                .lastObserved(observed)
                .build();
    }

    @Test
    void findPathReturnsShortestPathWithRelationshipTypes() {
        Instant t = Instant.parse("2026-08-12T21:15:00Z");
        List<RelationshipEntity> graph = List.of(
                relationship("REL001", "P001", "P002", "CALLED", t),
                relationship("REL002", "P002", "P003", "MET", t),
                relationship("REL003", "P003", "P008", "TRANSFERRED_TO", t));
        when(relationshipRepository.findAll()).thenReturn(graph);

        NetworkPathResponse response = networkService.findPath("P001", "P008", 5);

        org.assertj.core.api.Assertions.assertThat(response.pathFound()).isTrue();
        org.assertj.core.api.Assertions.assertThat(response.source()).isEqualTo("P001");
        org.assertj.core.api.Assertions.assertThat(response.target()).isEqualTo("P008");
        org.assertj.core.api.Assertions.assertThat(response.paths()).isNotEmpty();
        NetworkPathSegment segment = response.paths().get(0);
        org.assertj.core.api.Assertions.assertThat(segment.nodes()).containsExactly("P001", "P002", "P003", "P008");
        org.assertj.core.api.Assertions.assertThat(segment.relationships()).containsExactly("CALLED", "MET", "TRANSFERRED_TO");
        org.assertj.core.api.Assertions.assertThat(segment.length()).isEqualTo(3);
    }

    @Test
    void buildNetworkReturnsRootAndDirectNeighborsWithinDepth() {
        Instant t = Instant.parse("2026-08-12T21:15:00Z");
        List<RelationshipEntity> graph = List.of(
                relationship("REL001", "P001", "P002", "CALLED", t),
                relationship("REL002", "P002", "P003", "MET", t));
        when(relationshipRepository.findAll()).thenReturn(graph);
        when(personRepository.findById("P001"))
                .thenReturn(Optional.of(PersonEntity.builder().personId("P001").name("Rahul Sharma").status("ACTIVE").build()));
        when(personRepository.findById("P002"))
                .thenReturn(Optional.of(PersonEntity.builder().personId("P002").name("Amit Kumar").status("ACTIVE").build()));
        when(personRepository.findById("P003"))
                .thenReturn(Optional.of(PersonEntity.builder().personId("P003").name("Neha Singh").status("ACTIVE").build()));

        NetworkResponse response = networkService.getEntityNetwork("P001", 1);

        org.assertj.core.api.Assertions.assertThat(response.rootEntity()).isEqualTo("P001");
        org.assertj.core.api.Assertions.assertThat(response.nodes())
                .extracting(node -> node.id())
                .containsExactlyInAnyOrder("P001", "P002");
        org.assertj.core.api.Assertions.assertThat(response.edges()).hasSize(1);
    }
}