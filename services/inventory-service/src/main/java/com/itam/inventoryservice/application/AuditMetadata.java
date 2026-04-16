package com.itam.inventoryservice.application;

public record AuditMetadata(String actor, String correlationId) {
}
