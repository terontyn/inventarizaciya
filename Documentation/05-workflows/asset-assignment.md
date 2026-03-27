# Asset Assignment Workflow

## Цель
Назначить актив конкретному сотруднику с историей и аудитом.

## Preconditions
- актив существует
- пользователь существует
- оба принадлежат одному tenant
- актив доступен для назначения

## Steps
1. проверить tenant
2. проверить статус актива
3. создать assignment record
4. обновить current assignee
5. записать audit event

## Exceptions
- user not found
- asset not found
- tenant mismatch
- asset unavailable