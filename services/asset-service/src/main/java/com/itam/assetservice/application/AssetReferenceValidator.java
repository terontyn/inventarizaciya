package com.itam.assetservice.application;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetReferenceValidator {

    private final UserServiceReferenceLookup userServiceReferenceLookup;

    public AssetReferenceValidator(UserServiceReferenceLookup userServiceReferenceLookup) {
        this.userServiceReferenceLookup = userServiceReferenceLookup;
    }

    public void requireUserInSameTenant(UUID tenantId, UUID userId, String fieldName) {
        if (userId == null) {
            return;
        }
        try {
            if (!userServiceReferenceLookup.userExists(tenantId, userId)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must reference a user in the same tenant"
                );
            }
        } catch (ReferenceLookupUnavailableException exception) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to validate " + fieldName + " via user-service",
                exception
            );
        }
    }

    public void requireLocationInSameTenant(UUID tenantId, UUID locationId, String fieldName) {
        if (locationId == null) {
            return;
        }
        try {
            if (!userServiceReferenceLookup.locationExists(tenantId, locationId)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + " must reference a location in the same tenant"
                );
            }
        } catch (ReferenceLookupUnavailableException exception) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Unable to validate " + fieldName + " via user-service",
                exception
            );
        }
    }
}
