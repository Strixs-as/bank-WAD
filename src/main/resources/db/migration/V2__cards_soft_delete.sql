-- Add soft-delete fields to cards with DEFAULTs (SQL Server требует DEFAULT / nullable для не пустых таблиц)

IF COL_LENGTH('cards', 'is_deleted') IS NULL
BEGIN
    -- SQL Server позволяет добавить NOT NULL колонку в непустую таблицу, если сразу задан DEFAULT.
    -- Так избегаем отдельного UPDATE, который может валидироваться до выполнения ADD в некоторых режимах.
    ALTER TABLE cards ADD is_deleted bit NOT NULL CONSTRAINT DF_cards_is_deleted DEFAULT (0);
END

IF COL_LENGTH('cards', 'deleted_at') IS NULL
BEGIN
    ALTER TABLE cards ADD deleted_at datetime2 NULL;
END

IF COL_LENGTH('cards', 'deleted_reason') IS NULL
BEGIN
    ALTER TABLE cards ADD deleted_reason nvarchar(255) NULL;
END

IF COL_LENGTH('cards', 'deleted_by') IS NULL
BEGIN
    ALTER TABLE cards ADD deleted_by nvarchar(80) NULL;
END

