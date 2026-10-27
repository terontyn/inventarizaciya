# ADR-0003 Оргструктура Foundation Slice

Status: accepted
Date: 2026-03-27

## Context
В source of truth нет отдельной сущности Org.
Вместо нее есть Department, Branch, Location.
Нужно зафиксировать минимальную модель без искусственной универсальной иерархии.

## Options
1. Одна строгая иерархия: Branch → Department → Location
2. Две оси: Department как орг-ось, Branch / Location как физическая ось
3. Универсальное дерево org nodes

## Decision
Для Foundation Slice принимается:
- отдельной сущности Org нет;
- Department — организационная / управленческая ось;
- Branch — физическая площадка;
- Location — физическая локация внутри Branch;
- Location принадлежит Branch;
- Department не является родителем Location;
- Asset ссылается на Location, а не на Branch напрямую;
- User может быть связан с Department;
- обязательная прямая привязка User к Branch / Location в MVP не требуется.

## Consequences
Плюсы:
- минимальная и понятная модель;
- нет выдуманной “универсальной орг-иерархии”;
- проще разнести managerial и physical dimensions.

Минусы:
- later может понадобиться расширение модели workplace / location;
- часть сложных отчетных и security-сценариев будет вводиться позже.

## Rejected For MVP
- отдельная сущность Org
- универсальное дерево org nodes
- жесткая единая parent-child иерархия для всех org dimensions