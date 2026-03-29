package ru.yandex.practicum.exception;

public class NotEnoughInfoOrderException extends RuntimeException {
    public NotEnoughInfoOrderException(String message) {
        super(message);
    }
}
