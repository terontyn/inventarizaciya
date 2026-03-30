# Bounded Contexts

Status: approved
Owner: architecture
Last Updated: 2026-03-27

## Identity / Organization
Service owner: user-service

Entities:
- Tenant
- User
- Role
- Department
- Branch
- Location

Clarifications:
- отдельной сущности Org в Foundation Slice нет;
- Department — организационная / управленческая ось;
- Branch и Location — физическая ось;
- Location принадлежит Branch;
- Permissions описываются как часть role model, но direct user permissions в Foundation Slice не поддерживаются.

## Asset Management
Service owner: asset-service

Entities:
- Asset
- AssetAssignment
- AssetTransfer
- AssetRetirement

Clarifications:
- в Foundation Slice v1 публичный CRUD покрывает только мастер-запись Asset;
- AssetAssignment / AssetTransfer / AssetRetirement остаются частью asset-domain, но реализуются как отдельные flows, а не как generic PATCH.

## Cross-Context Rules
- между bounded contexts передаются только reference IDs;
- прямое чтение чужой БД запрещено;
- asset-service хранит tenant-scoped reference IDs на user/org entities;
- валидация assigned_user_id и location_id выполняется через API user-service;
- cross-context checks всегда выполняются в tenant scope.