package com.example.blpslab1.exceptions;

public class FileAlreadyExistsException extends RuntimeException{

    public FileAlreadyExistsException(String message) {
        super(message);
    }
    public FileAlreadyExistsException() {
    }

}
