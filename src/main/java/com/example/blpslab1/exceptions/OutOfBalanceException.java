package com.example.blpslab1.exceptions;

public class OutOfBalanceException extends RuntimeException{

    public OutOfBalanceException(String message) {
        super(message);
    }

    public OutOfBalanceException() {
    }
}
