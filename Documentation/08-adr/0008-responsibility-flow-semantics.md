# ADR-0008 Responsibility Flow Semantics

Status: accepted
Date: 2026-04-01

## Context

В Foundation Slice уже зафиксировано следующее:

- generic Asset CRUD управляет только мастер-записью Asset;
- `assigned_user_id` и `location_id` исключены из generic PATCH;
- `assign / transfer / return / retire` являются отдельными domain flows;
- история ответственности является append-only;
- критичные write-операции обязаны оставлять durable local audit record в той же транзакции с асинхронной доставкой в central audit storage.

В `asset-domain.md` уже определены канонические границы между responsibility flows:

- `Assignment` используется для установления текущей пользовательской ответственности;
- `Transfer` используется для изменения текущей пользовательской ответственности и/или текущей операционной локации как одного непрерывного доменного изменения;
- `Return` используется для завершения текущей пользовательской ответственности без назначения нового пользователя в том же flow;
- `Retirement` используется для вывода Asset из эксплуатации с сохранением audit и истории.

При этом в workflow docs остаются открытые решения, которые нужно закрыть до следующего flow-slice:

- должен ли `return` менять `location_id`;
- нужен ли `target location` для `return`;
- какой post-return `status` является каноническим;
- допустим ли `transfer` для актива без текущего assigned user;
- являются ли user-only transfer и location-only transfer одним flow или разными flow;
- обязателен ли `return` перед `retirement`;
- как `retirement` влияет на текущую ответственность и `location_id`;
- нужна ли отдельная canonical entity `AssetReturn`.

## Decision

### 1. Return semantics

Для MVP принимается:

- `Return` завершает только текущую пользовательскую ответственность.
- `Return` не канонизирует изменение `location_id`.
- `Return` не требует `target location`.

### 2. Post-return state

Для MVP канонизируется только следующее:

- после `Return` у Asset больше нет активной текущей пользовательской ответственности.

При этом:

- конкретное значение поля `status` для post-return состояния в этом ADR не канонизируется;
- `status` vocabulary остается отдельным open decision.

### 3. Transfer semantics

Для MVP принимается:

- `Transfer` является одним каноническим responsibility flow;
- внутри него допустимы три варианта доменного изменения:
  - user-only transfer;
  - location-only transfer;
  - combined user + location transfer.

Дополнительная boundary rule:

- если у Asset нет текущей пользовательской ответственности, операция, которая создает новую текущую пользовательскую ответственность, должна рассматриваться как `Assignment`, а не `Transfer`;
- `Transfer` без текущего assigned user допустим только для location-only scenario.

### 4. Retirement semantics

Для MVP принимается:

- предварительный `Return` перед `Retirement` не является обязательным precondition;
- `Retirement` завершает эксплуатационное использование Asset;
- если на момент `Retirement` у Asset существует текущая активная пользовательская ответственность, после `Retirement` она больше не считается активной;
- `Retirement` не канонизирует обязательное изменение `location_id`;
- конкретное retirement-related значение `status` в этом ADR не канонизируется.

### 5. History model

Для MVP принимается:

- `Return` обязан создавать отдельный append-only history fact;
- отдельная canonical entity `AssetReturn` в этом ADR не вводится;
- `Return` должен быть различим в истории как самостоятельный доменный факт, отличный от `Assignment`, `Transfer` и `Retirement`;
- финальная physical storage model history остается отдельным последующим решением.

## Consequences

### Positive

- уменьшается размывание границ между `Assignment`, `Transfer`, `Return` и `Retirement`;
- `Return` остается узким flow завершения ответственности и не смешивается с relocation semantics;
- `Transfer` остается единым flow без искусственного дробления на несколько top-level workflow;
- `Retirement` не требует искусственного обязательного шага `Return`, если бизнес-намерение состоит именно в выводе из эксплуатации;
- следующий flow-slice можно проектировать без выдумывания public API и payload schema;
- history semantics для `Return` фиксируются без преждевременной фиксации новой сущности или финальной storage schema.

### Negative

- точный словарь `status` остается открытым;
- точные доменные условия eligibility для `return`, `assignment` и `retirement` остаются частично открытыми;
- финальная physical history model остается открытой;
- некоторые операционные сценарии по-прежнему будут требовать двух отдельных доменных фактов вместо одного.

## Explicitly Rejected For This ADR

В рамках этого ADR не принимаются следующие решения:

- обязательный `target location` для `Return`;
- автоматическое каноническое изменение `location_id` в `Return`;
- правило, что любой `Retirement` обязан предварительно проходить через `Return`;
- введение отдельной canonical entity `AssetReturn` без отдельно зафиксированной необходимости;
- фиксация exact `status` values для post-return или retirement state.

## Out of Scope

Этот ADR намеренно не канонизирует:

- public command endpoints;
- request / response payload schema;
- error model;
- final history storage schema;
- delete semantics;
- точный enum для `status`;
- event contract beyond already accepted audit durability rule.

## Impact on Existing Docs

После принятия этого ADR должны быть обновлены:

- `/Documentation/02-domain/asset-domain.md`
- `/Documentation/05-workflows/asset-transfer.md`
- `/Documentation/05-workflows/asset-return.md`
- `/Documentation/05-workflows/asset-retirement.md`
- `/Documentation/03-data/asset-er.md`

## Canonical Clarifications To Propagate

### For asset-domain.md

Нужно добавить/уточнить:

- `Return` завершает текущую пользовательскую ответственность и не канонизирует relocation;
- relocation после `Return` должен трактоваться как отдельный доменный факт, а не как неявная часть `Return`;
- `Transfer` является единым каноническим flow с user-only, location-only и combined variants;
- создание новой текущей пользовательской ответственности при отсутствии текущей ответственности относится к `Assignment`, а не к `Transfer`;
- `Retirement` не требует обязательного pre-return, но завершает active operational use и не оставляет текущую пользовательскую ответственность активной.

### For asset-return.md

Нужно закрыть open decisions так:

- `Return` не меняет `location_id` канонически;
- `Return` не требует `target location`;
- post-return semantics = нет активной текущей пользовательской ответственности;
- exact post-return `status` value remains open;
- отдельная canonical entity `AssetReturn` не вводится, но отдельный history fact обязателен.

### For asset-transfer.md

Нужно закрыть open decisions так:

- `Transfer` — один канонический flow;
- user-only, location-only и combined change являются допустимыми вариантами одного flow;
- `Transfer` без текущего assigned user допустим только для location-only scenario;
- создание новой текущей пользовательской ответственности без текущего assigned user относится к `Assignment`.

### For asset-retirement.md

Нужно закрыть open decisions так:

- предварительный `Return` перед `Retirement` не обязателен;
- после `Retirement` текущая пользовательская ответственность, если она существовала, больше не считается активной;
- `Retirement` не канонизирует обязательное изменение `location_id`;
- exact retirement-related `status` value remains open.

### For asset-er.md

Нужно уточнить:

- отдельная canonical entity/table `AssetReturn` пока не вводится;
- history model должна уметь хранить distinguishable append-only fact type для `Return`;
- final physical storage schema history остается open.

## Follow-up Note

Следующим отдельным решением при необходимости должны фиксироваться:

- `status` vocabulary;
- eligibility rules для `assignment / return / retirement`;
- physical history storage model;
- public responsibility-flow API contracts.