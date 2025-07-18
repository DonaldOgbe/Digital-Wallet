package com.deodev.userService.repository;

import com.deodev.userService.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private RoleRepository testRoleRepository;


    @ParameterizedTest()
    @ValueSource(strings = {"ROLE_ADMIN", "ROLE_USER"})
    void findByName(String value) {

        if (testRoleRepository.findByName(value).isEmpty()) {
            testRoleRepository.save(new Role(null, value));
        }

        Role role = testRoleRepository.findByName(value).orElseThrow();

        assertEquals(value, role.getName());
    }
}