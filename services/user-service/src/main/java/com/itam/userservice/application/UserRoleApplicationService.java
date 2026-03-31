package com.itam.userservice.application;

import com.itam.userservice.domain.AuditAction;
import com.itam.userservice.domain.AuditEntity;
import com.itam.userservice.domain.RoleRepository;
import com.itam.userservice.domain.UserRepository;
import com.itam.userservice.domain.UserRole;
import com.itam.userservice.domain.UserRoleRepository;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserRoleApplicationService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditWriter auditWriter;

    public UserRoleApplicationService(
        UserRoleRepository userRoleRepository,
        UserRepository userRepository,
        RoleRepository roleRepository,
        AuditWriter auditWriter
    ) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditWriter = auditWriter;
    }

    @Transactional
    public UserRole bind(UUID tenantId, UUID userId, UUID roleId, AuditMetadata auditMetadata) {
        requireUserInSameTenant(tenantId, userId);
        requireRoleInSameTenant(tenantId, roleId);

        try {
            UserRole userRole = userRoleRepository.create(tenantId, userId, roleId);
            auditWriter.record(
                tenantId,
                auditMetadata,
                AuditEntity.USER_ROLE,
                bindingEntityId(tenantId, userId, roleId),
                AuditAction.CREATE,
                new UserRoleAuditPayload(userId, roleId)
            );
            return userRole;
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User role binding already exists", exception);
        }
    }

    private void requireUserInSameTenant(UUID tenantId, UUID userId) {
        if (!userRepository.exists(tenantId, userId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "user_id must reference a user in the same tenant"
            );
        }
    }

    private void requireRoleInSameTenant(UUID tenantId, UUID roleId) {
        if (!roleRepository.exists(tenantId, roleId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "role_id must reference a role in the same tenant"
            );
        }
    }

    private UUID bindingEntityId(UUID tenantId, UUID userId, UUID roleId) {
        String rawBindingKey = tenantId + ":" + userId + ":" + roleId;
        return UUID.nameUUIDFromBytes(rawBindingKey.getBytes(StandardCharsets.UTF_8));
    }

    private record UserRoleAuditPayload(UUID userId, UUID roleId) {
    }
}
