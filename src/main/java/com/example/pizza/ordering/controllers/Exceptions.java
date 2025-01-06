package com.example.pizza.ordering.controllers;

public class Exceptions {

    static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    static class AlreadyExistsException extends RuntimeException {
        AlreadyExistsException(String message) {
            super(message);
        }
    }

    static class BadRequestException extends RuntimeException {
        BadRequestException(String message) {
            super(message);
        }
    }

}
