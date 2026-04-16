package com.itam.inventoryservice.application;

public class ReferenceLookupUnavailableException extends RuntimeException {

    public ReferenceLookupUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
