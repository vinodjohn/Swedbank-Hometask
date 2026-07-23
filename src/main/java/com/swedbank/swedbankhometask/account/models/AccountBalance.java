package com.swedbank.swedbankhometask.account.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swedbank.swedbankhometask.common.models.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * The balance held by an {@link Account} in a single {@link Currency}.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "currency"}))
@EqualsAndHashCode(callSuper = true)
public final class AccountBalance extends Auditable<String> {
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
