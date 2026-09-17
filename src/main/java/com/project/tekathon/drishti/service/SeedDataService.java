package com.project.tekathon.drishti.service;

import com.project.tekathon.drishti.dto.CaseDtos.CreateCaseRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateCdrRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateLocationRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateTransactionRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateVehicleRequest;
import com.project.tekathon.drishti.entity.DocumentEntity;
import com.project.tekathon.drishti.entity.AccountEntity;
import com.project.tekathon.drishti.entity.PersonEntity;
import com.project.tekathon.drishti.entity.PhoneEntity;
import com.project.tekathon.drishti.entity.RelationshipEntity;
import com.project.tekathon.drishti.repository.CaseRepository;
import com.project.tekathon.drishti.repository.AccountRepository;
import com.project.tekathon.drishti.repository.DocumentRepository;
import com.project.tekathon.drishti.repository.PersonRepository;
import com.project.tekathon.drishti.repository.PhoneRepository;
import com.project.tekathon.drishti.repository.RelationshipRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SeedDataService {

    @Bean
    CommandLineRunner seed(CaseService caseService,
            PersonService personService,
            InvestigationDataService investigationDataService,
            NetworkService networkService,
            DocumentRepository documentRepository,
            RelationshipRepository relationshipRepository,
            PersonRepository personRepository,
            PhoneRepository phoneRepository,
            AccountRepository accountRepository,
            CaseRepository caseRepository) {
        return args -> {
            if (caseRepository.count() > 0 || personRepository.count() > 0) {
                return;
            }

            var case1 = caseService.create(new CreateCaseRequest("Sector 17 Investigation",
                    "Investigation involving multiple connected individuals", "OPEN"));
            var case2 = caseService.create(new CreateCaseRequest("Financial Network Probe",
                    "Suspicious money movement across linked accounts", "OPEN"));
            var case3 = caseService.create(new CreateCaseRequest("Surveillance Follow-up",
                    "Vehicle and location observation follow-up", "REVIEW_REQUIRED"));

            List<PersonEntity> people = List.of(
                    seededPerson("P001", "Rahul Sharma", List.of("Rahul S."), 34, "Sector 17", "ACTIVE"),
                    seededPerson("P002", "Amit Kumar", List.of(), 36, "Sector 22", "ACTIVE"),
                    seededPerson("P003", "Neha Singh", List.of(), 31, "Sector 15", "ACTIVE"),
                    seededPerson("P004", "Vikram Patel", List.of(), 41, "Manimajra", "REVIEW_REQUIRED"),
                    seededPerson("P005", "Sanjay Verma", List.of(), 38, "Mohali", "ACTIVE"),
                    seededPerson("P006", "Priya Rao", List.of("Priya R."), 29, "Chandigarh", "ACTIVE"),
                    seededPerson("P007", "Arjun Mehta", List.of(), 44, "Panchkula", "ACTIVE"),
                    seededPerson("P008", "Karan Gill", List.of(), 27, "Zirakpur", "ACTIVE"),
                    seededPerson("P009", "Simran Kaur", List.of(), 33, "Sector 17", "ACTIVE"),
                    seededPerson("P010", "Deepak Joshi", List.of(), 46, "Kharar", "REVIEW_REQUIRED"));
            people.forEach(personService::update);

            phoneRepository.saveAll(List.of(
                    PhoneEntity.builder().phoneId("PH001").phoneNumber("9876543210").ownerPersonId("P001").status("ACTIVE").build(),
                    PhoneEntity.builder().phoneId("PH002").phoneNumber("9123456789").ownerPersonId("P002").status("ACTIVE").build(),
                    PhoneEntity.builder().phoneId("PH003").phoneNumber("9000000003").ownerPersonId("P003").status("ACTIVE").build(),
                    PhoneEntity.builder().phoneId("PH004").phoneNumber("9000000004").ownerPersonId("P004").status("ACTIVE").build(),
                    PhoneEntity.builder().phoneId("PH005").phoneNumber("9000000005").ownerPersonId("P005").status("ACTIVE").build()));
            phoneRepository.findAll().forEach(networkService::syncPhone);

            accountRepository.saveAll(List.of(
                    AccountEntity.builder().accountId("ACC101").accountNumber("ACC101").accountType("SAVINGS").ownerPersonId("P001").institution("Bank A").build(),
                    AccountEntity.builder().accountId("ACC205").accountNumber("ACC205").accountType("CURRENT").ownerPersonId("P002").institution("Bank B").build(),
                    AccountEntity.builder().accountId("ACC301").accountNumber("ACC301").accountType("SAVINGS").ownerPersonId("P003").institution("Bank C").build(),
                    AccountEntity.builder().accountId("ACC401").accountNumber("ACC401").accountType("SAVINGS").ownerPersonId("P004").institution("Bank D").build(),
                    AccountEntity.builder().accountId("ACC501").accountNumber("ACC501").accountType("CURRENT").ownerPersonId("P005").institution("Bank E").build()));
            accountRepository.findAll().forEach(networkService::syncAccount);

            investigationDataService.createLocation(new CreateLocationRequest("Sector 17", "Sector 17, Chandigarh", 30.7415, 76.7681));
            investigationDataService.createLocation(new CreateLocationRequest("Sector 22", "Sector 22, Chandigarh", 30.7300, 76.7800));
            investigationDataService.createLocation(new CreateLocationRequest("Manimajra", "Manimajra, Chandigarh", 30.7420, 76.8290));
            investigationDataService.createLocation(new CreateLocationRequest("Mohali", "Mohali, Punjab", 30.7046, 76.7179));
            investigationDataService.createLocation(new CreateLocationRequest("Panchkula", "Panchkula, Haryana", 30.6942, 76.8606));

            investigationDataService.createVehicle(new CreateVehicleRequest("PB10AB1234", "BMW", "X5", "P002"));
            investigationDataService.createVehicle(new CreateVehicleRequest("PB10CD5678", "Toyota", "Corolla", "P004"));
            investigationDataService.createVehicle(new CreateVehicleRequest("HR26EF9012", "Honda", "City", "P006"));
            investigationDataService.createVehicle(new CreateVehicleRequest("PB65GH3456", "Maruti", "Swift", "P001"));
            investigationDataService.createVehicle(new CreateVehicleRequest("PB01JK7890", "Hyundai", "Creta", "P008"));

            // Five synthetic accounts, stored as direct entities for the MVP.
            relationshipRepository.save(RelationshipEntity.builder().relationshipId("REL001").caseId(case2.caseId())
                    .sourceId("P001").targetId("ACC101").relationship("OWNS").confidence(0.97)
                    .firstObserved(Instant.now()).lastObserved(Instant.now()).supportText("Seeded account link")
                    .evidenceIdsJson("[]").sourceDocumentIdsJson("[]").build());
            relationshipRepository.save(RelationshipEntity.builder().relationshipId("REL002").caseId(case2.caseId())
                    .sourceId("P002").targetId("ACC205").relationship("OWNS").confidence(0.97)
                    .firstObserved(Instant.now()).lastObserved(Instant.now()).supportText("Seeded account link")
                    .evidenceIdsJson("[]").sourceDocumentIdsJson("[]").build());

            cdrSeed(caseService, investigationDataService, case1.caseId());
            txSeed(investigationDataService, case2.caseId());

            documentRepository.save(DocumentEntity.builder()
                    .documentId("DOC001").caseId(case1.caseId()).documentType("FIR").title("FIR 238")
                    .text("Rahul Sharma was seen with Amit Kumar near Sector 17 on 12 August.")
                    .source("POLICE_DATABASE").language("en").build());
            documentRepository.save(DocumentEntity.builder()
                    .documentId("DOC002").caseId(case1.caseId()).documentType("POLICE_REPORT").title("Follow-up Report")
                    .text("Patrol observed a white BMW in the area.") .source("PATROL_REPORT").language("en").build());
            documentRepository.save(DocumentEntity.builder()
                    .documentId("DOC003").caseId(case2.caseId()).documentType("INTELLIGENCE_REPORT").title("Finance Note")
                    .text("Transfers were observed between linked accounts.") .source("INTELLIGENCE").language("en").build());
            documentRepository.save(DocumentEntity.builder()
                    .documentId("DOC004").caseId(case3.caseId()).documentType("SURVEILLANCE_REPORT").title("Surveillance Log")
                    .text("Vehicle observed near Sector 17.") .source("SURVEILLANCE").language("en").build());
            documentRepository.save(DocumentEntity.builder()
                    .documentId("DOC005").caseId(case3.caseId()).documentType("WITNESS_STATEMENT").title("Witness Statement")
                    .text("Witness described a meeting near Sector 17.") .source("WITNESS").language("en").build());

            relationshipRepository.save(RelationshipEntity.builder().relationshipId("REL003").caseId(case1.caseId())
                    .sourceId("P001").targetId("P002").relationship("MET").confidence(0.91)
                    .firstObserved(Instant.parse("2026-08-12T22:30:00Z")).lastObserved(Instant.parse("2026-08-12T22:30:00Z"))
                    .supportText("Seen together near Sector 17").evidenceIdsJson("[]").sourceDocumentIdsJson("[\"DOC001\"]").build());
            relationshipRepository.save(RelationshipEntity.builder().relationshipId("REL004").caseId(case1.caseId())
                    .sourceId("P001").targetId("PH001").relationship("USED").confidence(0.99)
                    .firstObserved(Instant.parse("2026-08-12T21:15:00Z")).lastObserved(Instant.parse("2026-08-12T21:15:00Z"))
                    .supportText("Phone used by Rahul").evidenceIdsJson("[]").sourceDocumentIdsJson("[\"DOC001\"]").build());
            relationshipRepository.save(RelationshipEntity.builder().relationshipId("REL005").caseId(case2.caseId())
                    .sourceId("ACC101").targetId("ACC205").relationship("TRANSFERRED_TO").confidence(0.88)
                    .firstObserved(Instant.parse("2026-08-12T10:30:00Z")).lastObserved(Instant.parse("2026-08-12T10:30:00Z"))
                    .supportText("Financial transfer").evidenceIdsJson("[]").sourceDocumentIdsJson("[\"DOC003\"]").build());
            log.info("Seed data created");
        };
    }

    private PersonEntity seededPerson(String id, String name, List<String> aliases, Integer age, String address, String status) {
        return PersonEntity.builder()
                .personId(id)
                .name(name)
                .aliases(aliases)
                .age(age)
                .address(address)
                .status(status)
                .build();
    }

    private void cdrSeed(CaseService caseService, InvestigationDataService investigationDataService, String caseId) {
        investigationDataService.createCdr(new CreateCdrRequest("PH001", "PH002", Instant.parse("2026-08-12T21:15:00Z"), 420, caseId));
        investigationDataService.createCdr(new CreateCdrRequest("PH002", "PH003", Instant.parse("2026-08-12T21:20:00Z"), 120, caseId));
        investigationDataService.createCdr(new CreateCdrRequest("PH003", "PH004", Instant.parse("2026-08-12T21:25:00Z"), 60, caseId));
        investigationDataService.createCdr(new CreateCdrRequest("PH001", "PH003", Instant.parse("2026-08-13T21:15:00Z"), 300, caseId));
        investigationDataService.createCdr(new CreateCdrRequest("PH004", "PH005", Instant.parse("2026-08-13T22:15:00Z"), 210, caseId));
        investigationDataService.createCdr(new CreateCdrRequest("PH005", "PH001", Instant.parse("2026-08-14T08:15:00Z"), 180, caseId));
        investigationDataService.createCdr(new CreateCdrRequest("PH002", "PH005", Instant.parse("2026-08-14T09:15:00Z"), 240, caseId));
        investigationDataService.createCdr(new CreateCdrRequest("PH001", "PH004", Instant.parse("2026-08-14T10:15:00Z"), 60, caseId));
        investigationDataService.createCdr(new CreateCdrRequest("PH003", "PH001", Instant.parse("2026-08-14T11:15:00Z"), 90, caseId));
        investigationDataService.createCdr(new CreateCdrRequest("PH002", "PH004", Instant.parse("2026-08-14T12:15:00Z"), 150, caseId));
    }

    private void txSeed(InvestigationDataService investigationDataService, String caseId) {
        investigationDataService.createTransaction(new CreateTransactionRequest("ACC101", "ACC205", new BigDecimal("250000"), "INR",
                Instant.parse("2026-08-12T10:30:00Z"), "Transfer", caseId));
        investigationDataService.createTransaction(new CreateTransactionRequest("ACC205", "ACC101", new BigDecimal("50000"), "INR",
                Instant.parse("2026-08-12T11:30:00Z"), "Return", caseId));
        investigationDataService.createTransaction(new CreateTransactionRequest("ACC101", "ACC301", new BigDecimal("100000"), "INR",
                Instant.parse("2026-08-13T10:30:00Z"), "Payment", caseId));
        investigationDataService.createTransaction(new CreateTransactionRequest("ACC301", "ACC205", new BigDecimal("75000"), "INR",
                Instant.parse("2026-08-13T11:30:00Z"), "Settlement", caseId));
        investigationDataService.createTransaction(new CreateTransactionRequest("ACC205", "ACC401", new BigDecimal("330000"), "INR",
                Instant.parse("2026-08-14T09:30:00Z"), "Transfer", caseId));
        investigationDataService.createTransaction(new CreateTransactionRequest("ACC401", "ACC101", new BigDecimal("45000"), "INR",
                Instant.parse("2026-08-14T10:30:00Z"), "Transfer", caseId));
        investigationDataService.createTransaction(new CreateTransactionRequest("ACC205", "ACC501", new BigDecimal("120000"), "INR",
                Instant.parse("2026-08-14T11:30:00Z"), "Payment", caseId));
        investigationDataService.createTransaction(new CreateTransactionRequest("ACC501", "ACC101", new BigDecimal("210000"), "INR",
                Instant.parse("2026-08-14T12:30:00Z"), "Receipt", caseId));
        investigationDataService.createTransaction(new CreateTransactionRequest("ACC301", "ACC401", new BigDecimal("90000"), "INR",
                Instant.parse("2026-08-14T13:30:00Z"), "Transfer", caseId));
        investigationDataService.createTransaction(new CreateTransactionRequest("ACC401", "ACC501", new BigDecimal("150000"), "INR",
                Instant.parse("2026-08-14T14:30:00Z"), "Transfer", caseId));
    }
}
