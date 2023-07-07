package ru.kata.spring.boot_security.demo.util;

public class UserNotCreateUpdateException extends RuntimeException {
    public UserNotCreateUpdateException(String message) {
        super(message);
    }
}
