package ru.practicum.shareit.exception.exceptions;

public class UniquenessConflictException extends RuntimeException {
    public UniquenessConflictException(String message) {
        super(message);
    }
}
