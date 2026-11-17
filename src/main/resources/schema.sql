-- =========================================================================
-- SNOUTY: ESQUEMA RELACIONAL POSTGRESQL / SUPABASE
-- =========================================================================

DROP TABLE IF EXISTS solicitudes_adopcion CASCADE;
DROP TABLE IF EXISTS revisiones_veterinarias CASCADE;
DROP TABLE IF EXISTS animales CASCADE;
DROP TABLE IF EXISTS seguimientos CASCADE;
DROP TABLE IF EXISTS asignaciones CASCADE;
DROP TABLE IF EXISTS evidencias CASCADE;
DROP TABLE IF EXISTS reportes CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;

-- 1. Tabla de Usuarios
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    rol VARCHAR(30) NOT NULL CHECK (rol IN ('CIUDADANO', 'VOLUNTARIO', 'VETERINARIO', 'ADMINISTRADOR')),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 2. Tabla de Reportes de Hallazgo (Perros y Gatos)
CREATE TABLE reportes (
    id BIGSERIAL PRIMARY KEY,
    especie VARCHAR(20) NOT NULL CHECK (especie IN ('PERRO', 'GATO')),
    descripcion TEXT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO', 'ASIGNADO', 'EN_PROCESO', 'RESCATADO')),
    prioridad VARCHAR(20) NOT NULL CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA')),
    ciudadano_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    latitud DOUBLE PRECISION NOT NULL,
    longitud DOUBLE PRECISION NOT NULL,
    direccion_referencia VARCHAR(255) NOT NULL,
    motivo_rechazo TEXT
);

-- 3. Tabla de Evidencias Fotográficas
CREATE TABLE evidencias (
    id BIGSERIAL PRIMARY KEY,
    reporte_id BIGINT NOT NULL REFERENCES reportes(id) ON DELETE CASCADE,
    url_foto VARCHAR(500) NOT NULL,
    tipo VARCHAR(30) NOT NULL CHECK (tipo IN ('INICIAL', 'SEGUIMIENTO', 'CIERRE')),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion VARCHAR(300) NOT NULL
);

-- 4. Tabla de Asignaciones de Voluntarios
CREATE TABLE asignaciones (
    id BIGSERIAL PRIMARY KEY,
    reporte_id BIGINT NOT NULL REFERENCES reportes(id) ON DELETE CASCADE,
    voluntario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL CHECK (estado IN ('ACTIVA', 'COMPLETADA', 'CANCELADA')),
    CONSTRAINT uq_reporte_voluntario UNIQUE (reporte_id, voluntario_id)
);

-- 5. Tabla de Seguimientos de Rescate (Línea de Tiempo)
CREATE TABLE seguimientos (
    id BIGSERIAL PRIMARY KEY,
    reporte_id BIGINT NOT NULL REFERENCES reportes(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_nuevo VARCHAR(30) NOT NULL CHECK (estado_nuevo IN ('PENDIENTE', 'APROBADO', 'RECHAZADO', 'ASIGNADO', 'EN_PROCESO', 'RESCATADO')),
    observacion TEXT NOT NULL,
    url_evidencia VARCHAR(500)
);

-- 6. Tabla de Animales Rescatados (Perros y Gatos)
CREATE TABLE animales (
    id BIGSERIAL PRIMARY KEY,
    reporte_origen_id BIGINT NOT NULL UNIQUE REFERENCES reportes(id) ON DELETE RESTRICT,
    especie VARCHAR(20) NOT NULL CHECK (especie IN ('PERRO', 'GATO')),
    nombre_provisional VARCHAR(100) NOT NULL,
    edad_aproximada VARCHAR(50) NOT NULL,
    sexo VARCHAR(20) NOT NULL CHECK (sexo IN ('MACHO', 'HEMBRA', 'DESCONOCIDO')),
    tamano VARCHAR(20) NOT NULL CHECK (tamano IN ('PEQUENO', 'MEDIANO', 'GRANDE')),
    descripcion TEXT NOT NULL,
    url_foto VARCHAR(500) NOT NULL,
    estado_adopcion VARCHAR(35) NOT NULL CHECK (estado_adopcion IN ('EN_RECUPERACION', 'DISPONIBLE', 'EN_PROCESO_ADOPCION', 'ADOPTADO'))
);

-- 7. Tabla de Revisiones Veterinarias
CREATE TABLE revisiones_veterinarias (
    id BIGSERIAL PRIMARY KEY,
    animal_id BIGINT NOT NULL REFERENCES animales(id) ON DELETE CASCADE,
    veterinario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    diagnostico TEXT NOT NULL,
    observaciones TEXT NOT NULL,
    apto_adopcion BOOLEAN NOT NULL DEFAULT FALSE
);

-- 8. Tabla de Solicitudes de Adopción
CREATE TABLE solicitudes_adopcion (
    id BIGSERIAL PRIMARY KEY,
    animal_id BIGINT NOT NULL REFERENCES animales(id) ON DELETE CASCADE,
    ciudadano_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo TEXT NOT NULL,
    estado VARCHAR(25) NOT NULL CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    observacion_admin TEXT
);

-- Índices para optimización de mapa y filtros
CREATE INDEX idx_reportes_estado_especie ON reportes(estado, especie);
CREATE INDEX idx_reportes_coordenadas ON reportes(latitud, longitude);
CREATE INDEX idx_animales_estado_especie ON animales(estado_adopcion, especie);
CREATE INDEX idx_solicitudes_animal_estado ON solicitudes_adopcion(animal_id, estado);