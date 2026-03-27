# MVP Scope

Status: approved
Owner: product
Last Updated: 2026-03-27

## MVP Name
Asset Accountability MVP

## Goal
Первый релиз должен отвечать без Excel на вопрос:
что у нас есть, где это находится, за кем закреплено и кто это менял.

## In Scope
- tenant-aware foundation для данных и доступа;
- users, roles, departments, branches, locations;
- реестр активов;
- карточка актива с текущим владельцем / локацией / статусом;
- assign / transfer / return;
- audit и история критических действий;
- минимально необходимый admin layer для поддержки этого контура.

## Out of Scope
- ticket / service desk;
- inventory / warehouse;
- software licenses / SAM;
- maintenance & repair;
- reporting как отдельный модуль;
- сложная аналитика и heavy BI;
- расширенные интеграции;
- кастомные workflow / automation features.

## Foundation Slice vs MVP
Foundation Slice — минимальный вертикальный путь.
MVP 6–8 недель — тот же путь, доведенный до pilot-ready состояния.

## Value
Один tenant должен перестать вести базовый учет техники в таблицах
и начать видеть:
- кто чем пользуется;
- где находится актив;
- кто и когда менял критичные данные.