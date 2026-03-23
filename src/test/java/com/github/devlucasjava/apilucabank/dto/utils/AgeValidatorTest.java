package com.github.devlucasjava.apilucabank.dto.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AgeValidatorTest {

    private AgeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setup() {
        validator = new AgeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void shouldReturnTrueWhenAgeIsValid() {
        LocalDate birthDate = LocalDate.now().minusYears(20);

        boolean result = validator.isValid(birthDate, context);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenAgeIsUnder18() {
        LocalDate birthDate = LocalDate.now().minusYears(10);

        boolean result = validator.isValid(birthDate, context);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenBirthDateIsNull() {
        boolean result = validator.isValid(null, context);

        assertTrue(result);
    }
}