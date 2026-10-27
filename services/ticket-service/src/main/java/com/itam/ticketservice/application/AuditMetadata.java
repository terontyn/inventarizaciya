package com.itam.ticketservice.application;

public record AuditMetadata(String actor, String correlationId) {
}
