package com.example.blpslab1.exceptions;

public class SubAlreadyExistsException extends RuntimeException{
    public SubAlreadyExistsException(String message) {
        super(message);
    }
    public SubAlreadyExistsException() {
    }
}
