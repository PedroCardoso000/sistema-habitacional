package com.esteirahabitacional.architecture;

import com.esteirahabitacional.EsteiraHabitacionalApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTest {

    @Test
    void shouldRespectDeclaredModuleBoundaries() {
        ApplicationModules.of(EsteiraHabitacionalApplication.class).verify();
    }
}

