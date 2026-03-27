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

Note:
Permissions упомянуты в исходных материалах bounded context, но пока не включены
в минимальную Foundation-модель до отдельного ADR по role / permission model.

## Asset Management
Service owner: asset-service

Entities:
- Asset
- AssetAssignment
- AssetTransfer
- AssetRetirement

Note:
В Foundation Slice v1 полноценные workflow/API для AssetAssignment / AssetTransfer /
AssetRetirement не входят в generic CRUD и должны проектироваться отдельно.

## Cross-Context Rules
- между contexts передаем reference IDs;
- прямое чтение чужой БД запрещено;
- asset-service валидирует tenant-scoped ссылки на user/location через API user-service;
- взаимодействие сервисов — через API first, позже возможно API + events.