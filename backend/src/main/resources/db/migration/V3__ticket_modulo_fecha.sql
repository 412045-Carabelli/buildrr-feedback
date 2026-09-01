IF COL_LENGTH('ticket', 'modulo') IS NULL
  ALTER TABLE ticket ADD modulo NVARCHAR(255);
GO

IF COL_LENGTH('ticket', 'fecha') IS NULL
  ALTER TABLE ticket ADD fecha DATE;
GO

UPDATE ticket SET fecha = CAST(creado_en AS DATE) WHERE fecha IS NULL;
GO

ALTER TABLE ticket ALTER COLUMN fecha DATE NOT NULL;
GO
