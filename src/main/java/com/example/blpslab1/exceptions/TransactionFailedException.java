package com.example.blpslab1.exceptions;

public class TransactionFailedException extends RuntimeException{
    public TransactionFailedException(String message) {
        super(message);
    }
    public TransactionFailedException() {
    }
}
