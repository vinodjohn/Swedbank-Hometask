/**
 * Core banking module: accounts, per-currency balances and the credit, debit and exchange operations.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
@ApplicationModule(allowedDependencies = {"common", "integration :: api"})
package com.swedbank.swedbankhometask.account;

import org.springframework.modulith.ApplicationModule;
