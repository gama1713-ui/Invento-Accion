INSERT INTO usuario (documento_identidad, nombre, apellidos, email, telefono_contacto, rol) VALUES ('1000000000', 'Admin', 'Principal', 'admin@invento.local', '3000000000', 'SUPER_ADMIN');

INSERT INTO activo (id, nombre, categoria, ubicacion, unidad_medida, cantidad_actual, stock_minimo, etiquetas_qr, estado, creado_en)
VALUES (1, 'Cable HDMI 2m', 'Cables', 'Almacen Central', 'unidad', 50, 5, 'QR001', 'DISPONIBLE', CURRENT_TIMESTAMP());

INSERT INTO activo (id, nombre, categoria, ubicacion, unidad_medida, cantidad_actual, stock_minimo, etiquetas_qr, estado, creado_en)
VALUES (2, 'Laptop Dell 14', 'Computo', 'Sede Norte', 'unidad', 10, 2, 'QR002', 'DISPONIBLE', CURRENT_TIMESTAMP());
