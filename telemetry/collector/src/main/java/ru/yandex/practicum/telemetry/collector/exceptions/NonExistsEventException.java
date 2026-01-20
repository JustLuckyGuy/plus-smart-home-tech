package ru.yandex.practicum.telemetry.collector.exceptions;

public class NonExistsEventException extends RuntimeException {
    public NonExistsEventException(String message) {
        super(message);
    }
}
