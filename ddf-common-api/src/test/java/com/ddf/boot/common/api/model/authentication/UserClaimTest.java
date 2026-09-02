package com.ddf.boot.common.api.model.authentication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserClaimTest {

    @Test
    void builderShouldPreserveDefaultPropertiesMap() {
        UserClaim claim = UserClaim.builder().userId("1").username("tester").build();

        assertNotNull(claim.getProperties());
        assertTrue(claim.getProperties().isEmpty());
    }
}
