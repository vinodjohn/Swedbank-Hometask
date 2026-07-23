package com.swedbank.swedbankhometask.account.models;

import com.swedbank.swedbankhometask.common.models.Auditable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A bank account holding one {@link AccountBalance} per {@link Currency}, guarded by optimistic locking.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public final class Account extends Auditable<String> {
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String owner;

    @Version
    private Long version;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<AccountBalance> balances = new LinkedHashSet<>();

    /**
     * Returns the balance held in the given currency, if any.
     */
    public Optional<AccountBalance> balanceOf(Currency currency) {
        return balances.stream()
                .filter(balance -> balance.getCurrency() == currency)
                .findFirst();
    }
}
