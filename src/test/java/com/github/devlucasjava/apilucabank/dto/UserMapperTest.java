package com.github.devlucasjava.apilucabank.dto;

import com.github.devlucasjava.apilucabank.dto.mapper.UserMapper;
import com.github.devlucasjava.apilucabank.dto.response.UsersResponse;
import com.github.devlucasjava.apilucabank.model.Role;
import com.github.devlucasjava.apilucabank.model.Users;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void shouldUserToUserResponseSuccessfully() {

        Role role = new Role();
        role.setName("USER");

        Users user = Users.builder()
                .id(UUID.randomUUID())
                .firstName("Lucas")
                .lastName("Macedo")
                .email("lucas@lucas.com")
                .passport("123456")
                .birthDate(LocalDate.of(2000, 1, 1))
                .isActive(true)
                .isLocked(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(role)
                .build();

        UsersResponse response = userMapper.toUsersResponse(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals("Lucas", response.getFirstName());
        assertEquals("lucas@lucas.com", response.getEmail());
        assertEquals("Macedo", response.getLastName());
        assertEquals("USER", response.getRole());
        assertTrue(response.isActive());
        assertFalse(response.isLocked());
    }

    @Test
    void shouldHandleNullFields() {

        Users user = new Users();
        user.setId(UUID.randomUUID());

        UsersResponse response = userMapper.toUsersResponse(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertNull(response.getFirstName());
        assertNull(response.getEmail());
        assertNull(response.getRole());
    }
}