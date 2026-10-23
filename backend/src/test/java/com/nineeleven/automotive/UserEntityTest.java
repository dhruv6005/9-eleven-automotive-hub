package com.nineeleven.automotive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.nineeleven.automotive.user.UserEntity;
import org.junit.jupiter.api.Test;

class UserEntityTest {
    @Test
    void normalizesEmailForConsistentLookups() {
        UserEntity user = new UserEntity();
        user.setEmail("  Workshop.Owner@Example.COM  ");
        assertEquals("workshop.owner@example.com", user.getEmail());
    }

    @Test
    void preservesNullEmailForBeanValidationToRejectLater() {
        UserEntity user = new UserEntity();
        user.setEmail(null);
        assertNull(user.getEmail());
    }
}
