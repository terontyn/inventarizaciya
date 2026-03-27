- Отдельная сущность `Org` сейчас не вводится.
- `Department / Branch / Location` считаются tenant-scoped org references.
- Если позже будут приняты ADR по org hierarchy и role/permission model, этот файл нужно обновить.

## Open Questions
- кардинальность `User ↔ Role`;
- нужна ли отдельная сущность `Permissions` в MVP;
- как именно связаны `Department / Branch / Location`;
- нужна ли обязательная привязка `User` к `Department / Branch / Location`.

## Not Included Yet
Намеренно не добавлены поля вроде:
- `name`
- `code`
- `parent_id`
- `department_id`
- `branch_id`
- `permissions`

Они встречались в более раннем широком blueprint, но не подтверждены как каноническая минимальная модель Foundation Slice.