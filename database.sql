-- Crear la base de datos
-- H2 no necesita CREATE DATABASE, se crea automáticamente al conectar al archivo

-- Tabla para la información general del club
CREATE TABLE IF NOT EXISTS club_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    logo_url VARCHAR(255),
    photo1 VARCHAR(255),
    photo2 VARCHAR(255),
    facebook VARCHAR(255),
    instagram VARCHAR(255),
    twitter VARCHAR(255),
    header_background_color VARCHAR(7) DEFAULT '#f4f4f4',
    header_text_color VARCHAR(7) DEFAULT '#000000',
    background_image_url VARCHAR(255),
    youtube_live_url VARCHAR(255)
);

-- Tabla para las noticias (Entradas)
CREATE TABLE IF NOT EXISTS post (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    content TEXT, -- TEXT permite guardar el contenido HTML generado
    image_url VARCHAR(255)
);

-- Tabla para los equipos
CREATE TABLE IF NOT EXISTS team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    category VARCHAR(255)
);

-- Tabla para los jugadores y cuerpo técnico
CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    role VARCHAR(255), -- "Jugador" o "Cuerpo Técnico"
    photo_url VARCHAR(255),
    team_id BIGINT,
    CONSTRAINT fk_member_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE
);

-- Tabla para usuarios del panel de administración
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Tabla para la gestión de imágenes subidas
CREATE TABLE IF NOT EXISTS image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255), -- Nombre original del archivo
    url VARCHAR(255)       -- URL relativa para acceso web (ej: /uploads/image.jpg)
);