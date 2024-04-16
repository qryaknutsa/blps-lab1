package com.example.blpslab1.exceptions;

public class UserAlreadyExistsException extends RuntimeException{


    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException() {
        String message = "Такой юзер уже существует";
    }
}
