package com.swedbank.swedbankhometask.account.repositories;

import com.swedbank.swedbankhometask.account.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data repository for {@link Account} aggregates.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
}
