package com.example.medicinedistribution.Exception;

public class DeleteFailedException extends RuntimeException {
    public DeleteFailedException(String message) {
        super(message);
    }
}
