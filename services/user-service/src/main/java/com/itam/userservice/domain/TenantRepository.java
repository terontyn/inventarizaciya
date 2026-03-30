package com.itam.userservice.domain;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository {

    Tenant create(String name);

    Optional<Tenant> findById(UUID id);
}
