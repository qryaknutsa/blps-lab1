package com.example.blpslab1.exceptions;

public class FileNotFoundException extends RuntimeException{
    public FileNotFoundException(String message) {
        super(message);
    }
    public FileNotFoundException() {

    }
}
