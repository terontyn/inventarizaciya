package com.itam.maintenanceservice.application;

public record AuditMetadata(String actor, String correlationId) {
}
