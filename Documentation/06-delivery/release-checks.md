# Проверки перед выпуском
Запустить Gradle test для семи сервисов, PostgreSQL upgrade с public V7/V2 и соответствующей legacy V14/V4, тесты tenant/role boundaries, lifecycle guards и UI сохранения после reload.
Не использовать рабочие базы и не запускать Flyway repair. Демо-данные запускаются только отдельным профилем. Полный статус приёмки указывается в PR, а не выводится из наличия workflow.
