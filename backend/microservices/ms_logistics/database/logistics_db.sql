-- =====================================================================
--  Logistics microservice - MySQL database
--  OPTIONAL: Hibernate (ddl-auto=update) already creates the tables.
--  Run this script if you want the schema + sample data:
--      mysql -u root -p < database/logistics_db.sql
--  (or paste it in phpMyAdmin / MySQL Workbench)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS logistics_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE logistics_db;

DROP TABLE IF EXISTS delivery;
DROP TABLE IF EXISTS warehouse;

-- ---------------------------------------------------------------------
--  Table: warehouse
-- ---------------------------------------------------------------------
CREATE TABLE warehouse (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    fridge_capacity DOUBLE       NOT NULL,
    normal_capacity DOUBLE       NOT NULL,
    address_locale  VARCHAR(255) NOT NULL,
    CONSTRAINT pk_warehouse PRIMARY KEY (id)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
--  Table: delivery
--  order_id is the id of an order owned by the Ordering microservice:
--  NO foreign key (one database per service).
--  There is also no relation between delivery and warehouse.
-- ---------------------------------------------------------------------
CREATE TABLE delivery (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    order_id      BIGINT       NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    dispatched_at DATETIME(6)  NULL,
    destination   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_delivery PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE INDEX idx_delivery_status   ON delivery (status);
CREATE INDEX idx_delivery_order_id ON delivery (order_id);

-- ---------------------------------------------------------------------
--  Sample data
-- ---------------------------------------------------------------------
INSERT INTO warehouse (name, fridge_capacity, normal_capacity, address_locale) VALUES
  ('Tunis Central Warehouse', 500.0, 2000.0, 'Zone Industrielle, Ben Arous'),
  ('Sfax Warehouse',          300.0, 1500.0, 'Route de Tunis km 4, Sfax');

INSERT INTO delivery (order_id, status, dispatched_at, destination) VALUES
  (101, 'PENDING',    NULL,                  '12 Avenue Habib Bourguiba, Tunis'),
  (102, 'DISPATCHED', '2026-11-10 09:30:00', 'Rue de Marseille, Sousse'),
  (103, 'DELIVERED',  '2026-11-08 14:00:00', 'Avenue de la Liberté, Sfax');
