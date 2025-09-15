package com.deodev.userService.repository;

import com.deodev.userService.dto.request.UserRegistrationRequest;
import com.deodev.userService.model.User;
import com.deodev.userService.enums.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository testUserRepository;

    @Autowired
    private EntityManager entityManager;

    private UserRegistrationRequest userRegistrationRequest;

    private User user;

    @BeforeEach
    void setUp() {
        userRegistrationRequest = UserRegistrationRequest.builder()
                .email("test@email.com")
                .firstname("firstname")
                .lastname("lastname")
                .password("4djddedsdjdd4d")
                .build();

        user = new ObjectMapper().convertValue(userRegistrationRequest, User.class);
    }

    @Test
    void existsByUsername() {
        testUserRepository.save(user);

        assertTrue(testUserRepository.existsByEmail(user.getEmail()));
    }

    @Test
    @DisplayName("User status should be updated to a new enum string value")
    void userStatusUpdated() {
        // given
        User savedUser = testUserRepository.save(user);

        // when
        boolean isUpdated = testUserRepository.updateUserStatus(savedUser.getId(), UserStatus.ACTIVE) == 1;
        entityManager.clear();

        // then
        assertTrue(isUpdated);

        User updatedUser = testUserRepository.findById(savedUser.getId()).orElseThrow();
        assertEquals(UserStatus.ACTIVE, updatedUser.getStatus());
    }
}