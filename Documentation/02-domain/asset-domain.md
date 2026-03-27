# Asset Domain

## Основные сущности
- Asset
- AssetAssignment
- AssetTransfer
- AssetRetirement

## Основные поля Asset
- asset_tag
- serial_number
- asset_type
- status
- assigned_user_id
- location_id

## Инварианты
- asset_tag уникален внутри tenant
- актив принадлежит tenant
- история назначения и перемещений не теряется