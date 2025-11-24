package ru.bellintegrator.users_service.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleNotFoundException_ShouldReturn404AndMessage() {
        String errorMessage = "User not found by ID: 123";
        NoSuchElementException exception = new NoSuchElementException(errorMessage);

        ResponseEntity<String> response = globalExceptionHandler.handleNotFoundException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void handleBadRequestException_ShouldReturn400AndMessage() {
        String errorMessage = "ID must be null for creation";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        ResponseEntity<String> response = globalExceptionHandler.handleBadRequestException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }
}