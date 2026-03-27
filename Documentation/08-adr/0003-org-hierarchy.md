# ADR-0003 Org Hierarchy

Status: proposed
Date: 2026-03-27

## Context
В Foundation Slice org-часть представлена как Department / Branch / Location.
Отдельной сущности Org в source docs нет.
Но точная иерархия между Department / Branch / Location пока не зафиксирована.

## Decision
Для MVP:
- не вводить отдельную сущность organizations;
- считать Department, Branch и Location tenant-scoped org references;
- не фиксировать жесткую иерархию parent-child до появления отдельного бизнес-требования;
- прямую обязательную привязку User к Department / Branch / Location не считать утвержденной в MVP.

## Why
- это не добавляет выдуманной сложности;
- соответствует текущему source of truth;
- не ломает Foundation Slice.

## Consequences
Плюсы:
- минимум выдуманных полей и связей;
- проще стартовая модель.

Минусы:
- полноценная орг-иерархия будет уточняться позже;
- некоторые отчеты и сложные права позже могут потребовать расширения модели.

## Follow-up
Обновить:
- /docs/01-architecture/bounded-contexts.md
- /docs/02-domain/user-domain.md
- /docs/03-data/user-er.md