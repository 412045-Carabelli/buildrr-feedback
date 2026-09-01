CREATE TABLE usuario_aplicacion (
  id BIGINT NOT NULL PRIMARY KEY IDENTITY(1,1),
  username NVARCHAR(100) NOT NULL,
  producto NVARCHAR(20) NOT NULL,
  rol NVARCHAR(20) NOT NULL,
  CONSTRAINT uq_usuario_aplicacion_username_producto UNIQUE (username, producto)
);

CREATE INDEX idx_usuario_aplicacion_username ON usuario_aplicacion(username);

-- Seed inicial. "frezco" es el username de la cuenta de TEST (sgo_auth_test) —
-- reemplazar por el real cuando la dueña de FrezCo tenga cuenta en sgo_auth (prod).
INSERT INTO usuario_aplicacion (username, producto, rol) VALUES
  ('pablo', 'SGO', 'CLIENTE'),
  ('frezco', 'FRESCO', 'CLIENTE'),
  ('gcarabelli', 'SGO', 'ADMIN'),
  ('gcarabelli', 'FRESCO', 'ADMIN');
