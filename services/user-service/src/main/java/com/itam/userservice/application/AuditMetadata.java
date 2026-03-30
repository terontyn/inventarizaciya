package com.itam.userservice.application;

public record AuditMetadata(String actor, String correlationId) {
}
