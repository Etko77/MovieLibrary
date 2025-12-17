package com.example.demo.exceptions;

public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(Long id) {
        super("Movie not found with id: " + id);
    }

    public MovieNotFoundException(String message) {
        super(message);
    }
}
