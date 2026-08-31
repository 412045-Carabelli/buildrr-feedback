CREATE TABLE ticket (
  id BIGINT NOT NULL PRIMARY KEY IDENTITY(1,1),
  tipo NVARCHAR(20) NOT NULL,
  producto NVARCHAR(20) NOT NULL,
  titulo NVARCHAR(255) NOT NULL,
  descripcion NVARCHAR(MAX),
  estado NVARCHAR(20) NOT NULL DEFAULT 'NUEVO',
  creado_por NVARCHAR(100) NOT NULL,
  creado_en DATETIME2 NOT NULL DEFAULT GETDATE(),
  ultima_actualizacion DATETIME2
);

CREATE INDEX idx_ticket_estado ON ticket(estado);
CREATE INDEX idx_ticket_producto ON ticket(producto);

CREATE TABLE historial_estado (
  id BIGINT NOT NULL PRIMARY KEY IDENTITY(1,1),
  ticket_id BIGINT NOT NULL REFERENCES ticket(id),
  estado_anterior NVARCHAR(20),
  estado_nuevo NVARCHAR(20) NOT NULL,
  nota NVARCHAR(MAX),
  cambiado_por NVARCHAR(100) NOT NULL,
  cambiado_en DATETIME2 NOT NULL DEFAULT GETDATE()
);

CREATE INDEX idx_historial_estado_ticket ON historial_estado(ticket_id);

CREATE TABLE adjunto (
  id BIGINT NOT NULL PRIMARY KEY IDENTITY(1,1),
  ticket_id BIGINT NOT NULL REFERENCES ticket(id),
  historial_estado_id BIGINT NULL REFERENCES historial_estado(id),
  tipo NVARCHAR(20) NOT NULL,
  url NVARCHAR(500) NOT NULL,
  subido_por NVARCHAR(100) NOT NULL,
  subido_en DATETIME2 NOT NULL DEFAULT GETDATE()
);

CREATE INDEX idx_adjunto_ticket ON adjunto(ticket_id);

CREATE TABLE registro_horas (
  id BIGINT NOT NULL PRIMARY KEY IDENTITY(1,1),
  ticket_id BIGINT NOT NULL REFERENCES ticket(id),
  horas DECIMAL(5,2) NOT NULL,
  fecha DATE NOT NULL,
  nota NVARCHAR(500),
  cargado_por NVARCHAR(100) NOT NULL
);

CREATE INDEX idx_registro_horas_fecha ON registro_horas(fecha);
