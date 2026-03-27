# Asset ER

## Таблица assets
- id UUID PK
- tenant_id UUID
- asset_tag TEXT
- serial_number TEXT
- asset_type TEXT
- status TEXT
- assigned_user_id UUID
- location_id UUID
- created_at
- updated_at

## Индексы
- unique (tenant_id, asset_tag)
- index (tenant_id, status)
- index (tenant_id, assigned_user_id)