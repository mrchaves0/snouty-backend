-- =========================================================================
-- SNOUTY: DATOS SEMILLA DE PRUEBA (CHÍA / CUNDINAMARCA)
-- Contraseña para todos los usuarios: Snouty123*
-- =========================================================================

INSERT INTO usuarios (id, nombre, correo, password_hash, telefono, rol, activo) VALUES
(1, 'Laura Gómez (Admin)', 'admin@snouty.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '3101112233', 'ADMINISTRADOR', true),
(2, 'Carlos Andrés Pérez (Voluntario)', 'voluntario@snouty.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '3152223344', 'VOLUNTARIO', true),
(3, 'Dra. Valentina Ruiz (Veterinaria)', 'veterinario@snouty.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '3203334455', 'VETERINARIO', true),
(4, 'Mateo Vargas (Ciudadano)', 'ciudadano@snouty.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '3004445566', 'CIUDADANO', true),
(5, 'Camila Torres (Ciudadana)', 'camila@snouty.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', '3015556677', 'CIUDADANO', true)
ON CONFLICT (id) DO NOTHING;

-- Reportes georreferenciados en Chía y Cajicá (Cundinamarca)
INSERT INTO reportes (id, especie, descripcion, fecha_creacion, estado, prioridad, ciudadano_id, latitud, longitud, direccion_referencia, motivo_rechazo) VALUES
(1, 'PERRO', 'Perro mestizo mediano color miel con cojera en pata delantera derecha, cerca del Parque Principal Santander.', '2025-05-01 08:30:00', 'RESCATADO', 'ALTA', 4, 4.8616, -74.0587, 'Parque Principal de Chía, frente a la Parroquia Santa Lucía', NULL),
(2, 'GATO', 'Gata tricolor juvenil refugiada debajo de una caseta de vigilancia bajo la lluvia, maúlla constantemente.', '2025-05-02 10:15:00', 'RESCATADO', 'CRITICA', 5, 4.8552, -74.0498, 'Universidad de Cundinamarca Sede Chía, entrada peatonal Av. Pradilla', NULL),
(3, 'PERRO', 'Cachorro negro desorientado corriendo cerca de la vía principal con riesgo de atropellamiento.', '2025-05-04 14:20:00', 'EN_PROCESO', 'CRITICA', 4, 4.8654, -74.0392, 'Centro Comercial Centro Chía, costado norte sobre Av. Pradilla', NULL),
(4, 'GATO', 'Gato adulto gris con collar roto en zona verde residencial, parece extraviado pero dócil.', '2025-05-05 09:00:00', 'APROBADO', 'MEDIA', 5, 4.8721, -74.0520, 'Vereda Bojacá, sector Tres Esquinas, Chía', NULL),
(5, 'PERRO', 'Perro tipo criollo durmiendo cerca de panadería local, sin signos de maltrato visibles.', '2025-05-06 11:45:00', 'PENDIENTE', 'BAJA', 4, 4.8588, -74.0631, 'Barrio Río Frío, calle 13 con carrera 12, Chía', NULL),
(6, 'GATO', 'Reporte duplicado de felino en tejado que ya pertenece a un vecino del sector.', '2025-05-03 16:00:00', 'RECHAZADO', 'BAJA', 4, 4.8601, -74.0550, 'Carrera 9 # 11-24, Centro de Chía', 'Se verificó con residentes de la cuadra que el gato tiene propietario y sale al tejado habitualmente.')
ON CONFLICT (id) DO NOTHING;

-- Evidencias fotográficas
INSERT INTO evidencias (id, reporte_id, url_foto, tipo, fecha, descripcion) VALUES
(1, 1, 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=800', 'INICIAL', '2025-05-01 08:31:00', 'Fotografía inicial tomada por el ciudadano en el Parque Principal.'),
(2, 1, 'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=800', 'CIERRE', '2025-05-01 15:20:00', 'Canino asegurado con correa y guacal en vehículo de rescate.'),
(3, 2, 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800', 'INICIAL', '2025-05-02 10:16:00', 'Gatita resguardada de la lluvia en portería UdeC.'),
(4, 2, 'https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=800', 'CIERRE', '2025-05-02 13:45:00', 'Felina rescatada en guacal térmico y trasladada a valoración.'),
(5, 3, 'https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?w=800', 'INICIAL', '2025-05-04 14:22:00', 'Cachorro avistado cerca del separador vial de Av. Pradilla.')
ON CONFLICT (id) DO NOTHING;

-- Asignaciones a Voluntarios
INSERT INTO asignaciones (id, reporte_id, voluntario_id, fecha_asignacion, estado) VALUES
(1, 1, 2, '2025-05-01 09:30:00', 'COMPLETADA'),
(2, 2, 2, '2025-05-02 10:45:00', 'COMPLETADA'),
(3, 3, 2, '2025-05-04 15:00:00', 'ACTIVA')
ON CONFLICT (id) DO NOTHING;

-- Seguimientos de Rescate
INSERT INTO seguimientos (id, reporte_id, usuario_id, fecha, estado_nuevo, observacion, url_evidencia) VALUES
(1, 1, 1, '2025-05-01 09:00:00', 'APROBADO', 'Reporte validado por administración. Se prioriza por lesión en extremidad.', NULL),
(2, 1, 1, '2025-05-01 09:30:00', 'ASIGNADO', 'Caso asignado al voluntario Carlos Andrés Pérez.', NULL),
(3, 1, 2, '2025-05-01 12:10:00', 'EN_PROCESO', 'En el punto de referencia. Se ofrece alimento húmedo para ganar confianza del canino.', 'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=800'),
(4, 1, 2, '2025-05-01 15:20:00', 'RESCATADO', 'Rescate exitoso. El perro permitió colocación de pretal y fue trasladado al refugio Snouty.', 'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=800'),
(5, 2, 1, '2025-05-02 10:30:00', 'APROBADO', 'Caso crítico aprobado de inmediato por hipotermia.', NULL),
(6, 2, 2, '2025-05-02 11:30:00', 'EN_PROCESO', 'Acercamiento con trampa humanitaria y manta térmica en entrada de la universidad.', NULL),
(7, 2, 2, '2025-05-02 13:45:00', 'RESCATADO', 'Gatita asegurada sin lesiones graves y llevada a clínica aliada.', 'https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=800'),
(8, 3, 2, '2025-05-04 16:10:00', 'EN_PROCESO', 'Búsqueda activa en zona verde perimetral de Centro Chía.', NULL)
ON CONFLICT (id) DO NOTHING;

-- Animales Rescatados (1 Perro disponible y 1 Gata en recuperación)
INSERT INTO animales (id, reporte_origen_id, especie, nombre_provisional, edad_aproximada, sexo, tamano, descripcion, url_foto, estado_adopcion) VALUES
(1, 1, 'PERRO', 'Max', '2 años', 'MACHO', 'MEDIANO', 'Perro mestizo noble, sociable con niños y otros animales. Recuperado totalmente de su esguince leve.', 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=800', 'DISPONIBLE'),
(2, 2, 'GATO', 'Mishi', '7 meses', 'HEMBRA', 'PEQUENO', 'Gatita calicó muy cariñosa y tranquila. Actualmente finalizando esquema de desparasitación y ganancia de peso.', 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800', 'EN_RECUPERACION')
ON CONFLICT (id) DO NOTHING;

-- Revisiones Veterinarias
INSERT INTO revisiones_veterinarias (id, animal_id, veterinario_id, fecha, diagnostico, observaciones, apto_adopcion) VALUES
(1, 1, 3, '2025-05-03 10:00:00', 'Esguince grado I en miembro anterior derecho resuelto. Hemograma normal.', 'Vacunación polivalente y antirrábica al día. Desparasitado y esterilizado. Apto para adopción inmediata.', true),
(2, 2, 3, '2025-05-03 11:30:00', 'Bajo peso leve y rinitis pasajera por exposición al frío.', 'En tratamiento nutricional por 7 días antes de habilitar adopción.', false)
ON CONFLICT (id) DO NOTHING;

-- Solicitudes de Adopción
INSERT INTO solicitudes_adopcion (id, animal_id, ciudadano_id, fecha, motivo, estado, observacion_admin) VALUES
(1, 1, 4, '2025-05-05 18:00:00', 'Vivo en casa con patio cerrado en Chía, trabajo desde casa y deseo brindarle un hogar definitivo a Max.', 'PENDIENTE', NULL)
ON CONFLICT (id) DO NOTHING;

-- Sincronizar secuencias de PostgreSQL tras inserción manual de IDs
SELECT setval('usuarios_id_seq', (SELECT COALESCE(MAX(id), 1) FROM usuarios));
SELECT setval('reportes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM reportes));
SELECT setval('evidencias_id_seq', (SELECT COALESCE(MAX(id), 1) FROM evidencias));
SELECT setval('asignaciones_id_seq', (SELECT COALESCE(MAX(id), 1) FROM asignaciones));
SELECT setval('seguimientos_id_seq', (SELECT COALESCE(MAX(id), 1) FROM seguimientos));
SELECT setval('animales_id_seq', (SELECT COALESCE(MAX(id), 1) FROM animales));
SELECT setval('revisiones_veterinarias_id_seq', (SELECT COALESCE(MAX(id), 1) FROM revisiones_veterinarias));
SELECT setval('solicitudes_adopcion_id_seq', (SELECT COALESCE(MAX(id), 1) FROM solicitudes_adopcion));