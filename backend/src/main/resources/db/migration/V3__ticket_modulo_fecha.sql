ALTER TABLE ticket ADD modulo NVARCHAR(255);
ALTER TABLE ticket ADD fecha DATE;

UPDATE ticket SET fecha = CAST(creado_en AS DATE) WHERE fecha IS NULL;

ALTER TABLE ticket ALTER COLUMN fecha DATE NOT NULL;
