package com.project.tekathon.drishti.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class TransactionEntity extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String transactionId;

    @Column(nullable = false)
    private String sourceAccountId;

    @Column(nullable = false)
    private String destinationAccountId;

    @Column(nullable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private String caseId;
}
