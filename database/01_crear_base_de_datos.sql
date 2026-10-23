-- Ejecutar una sola vez en phpMyAdmin o HeidiSQL.
CREATE DATABASE IF NOT EXISTS restaurante_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
-- Las tablas plato, pedido y pedido_plato las crea Spring Data JPA
-- al iniciar la aplicacion con spring.jpa.hibernate.ddl-auto=update.
