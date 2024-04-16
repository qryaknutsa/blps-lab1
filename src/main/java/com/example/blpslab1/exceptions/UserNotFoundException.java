package com.example.blpslab1.exceptions;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException() {
        String message = "Такого юзера нет";
    }

    public UserNotFoundException(String message) {
        super(message) ;
    }

}
