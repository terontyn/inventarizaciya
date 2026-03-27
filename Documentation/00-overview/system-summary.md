# System Summary

## Что это за система
IT Asset Management SaaS для учета техники, пользователей, перемещений, ремонтов, заявок, ПО и аудита.

## Для кого
- internal IT teams
- office administrators
- service desk
- auditors
- management

## Какие основные модули
- user-service
- asset-service
- ticket-service
- inventory-service
- software-service
- maintenance-service
- audit/reporting

## Текущий этап
Проектирование Foundation Slice и MVP.

## Что уже решено
- docs = source of truth
- архитектура проектируется отдельно от кода
- tenant isolation обязателен
- сервисы не читают БД друг друга
- взаимодействие через API/events

## Что пока открыто
- точная tenancy model
- точный состав MVP
- состав first release
- event bus choice