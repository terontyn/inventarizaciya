## Mandatory Repo Verification Before New Codex Task

Перед подготовкой любого нового ready-to-Codex implementation packet
нужно обязательно выполнять repo verification.

### Required check
Нужно явно ответить на вопрос:
`target slice / target feature already exists in repo or not?`

### If target already exists
Если целевой slice уже реализован в repo:
- нельзя формировать новый greenfield implementation packet;
- нужно переключаться на один из режимов:
  - review,
  - hardening,
  - gap-closure,
  - CI/test completion,
  - docs synchronization,
  - next-slice planning.

### If target is missing
Только если repo verification подтверждает,
что target slice действительно отсутствует,
допустимо формировать ready-to-Codex implementation packet
на реализацию этого slice.

### Anti-loop rule
Planning packet, old chat output или старый implementation plan
не считаются достаточным основанием для новой реализации,
если нет свежей проверки текущего состояния repo.

### Goal
Это правило нужно, чтобы не переоткрывать уже реализованные slices
и не ходить по кругу между docs, planning и Codex.