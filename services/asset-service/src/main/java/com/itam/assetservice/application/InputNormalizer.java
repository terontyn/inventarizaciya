package com.itam.assetservice.application;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class InputNormalizer {

    private InputNormalizer() {
    }

    public static String requiredTrimmed(String value, String fieldName) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must not be blank");
        }

        return normalized;
    }

    public static String optionalTrimmed(
        String value,
        String fieldName,
        int maxLength,
        boolean normalizeLineBreaks
    ) {
        if (value == null) {
            return null;
        }

        String normalized = normalizeLineBreaks ? value.replaceAll("\\R+", " ") : value;
        normalized = normalized.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > maxLength) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                fieldName + " must not exceed " + maxLength + " characters"
            );
        }

        return normalized;
    }
}
