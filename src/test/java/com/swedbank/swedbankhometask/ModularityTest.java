package com.swedbank.swedbankhometask;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Verifies that the Spring Modulith module boundaries are respected.
 *
 * @author vinodjohn
 * @since 25.07.2026
 */
class ModularityTest {
    private final ApplicationModules modules = ApplicationModules.of(SwedbankHometaskApplication.class);

    @Test
    @DisplayName("module structure honours the declared boundaries")
    void verifiesModuleStructure() {
        modules.verify();
    }
}
