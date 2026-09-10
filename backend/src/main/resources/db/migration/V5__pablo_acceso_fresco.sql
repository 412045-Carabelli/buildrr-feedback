-- V5: Pablo (SGO) tambien debe poder filar/ver tickets de FrezCo.
IF NOT EXISTS (SELECT 1 FROM usuario_aplicacion WHERE username = 'pablo' AND producto = 'FRESCO')
BEGIN
    INSERT INTO usuario_aplicacion (username, producto, rol) VALUES ('pablo', 'FRESCO', 'CLIENTE');
END
