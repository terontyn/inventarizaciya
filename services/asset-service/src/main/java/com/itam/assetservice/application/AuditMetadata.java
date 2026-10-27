package com.itam.assetservice.application;

public record AuditMetadata(String actor, String correlationId) {
}
