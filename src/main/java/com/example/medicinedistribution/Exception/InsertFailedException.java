// Language: java
package com.example.medicinedistribution.Exception;

public class InsertFailedException extends RuntimeException {
    public InsertFailedException(String message) {
        super(message);
    }
}