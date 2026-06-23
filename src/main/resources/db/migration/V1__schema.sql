-- =========================================================
-- CAFE INVENTORY MANAGEMENT SYSTEM
-- V1: Core schema (SQL First)
-- MySQL 8
-- =========================================================

-- ---------- USERS ----------
CREATE TABLE users (
    user_id      BIGINT       NOT NULL AUTO_INCREMENT,
    username     VARCHAR(100) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    full_name    VARCHAR(255),
    email        VARCHAR(255),
    role         VARCHAR(20)  NOT NULL DEFAULT 'VIEWER',
    active_flag  TINYINT      NOT NULL DEFAULT 1,
    created_by   VARCHAR(100),
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(100),
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- SUPPLIERS ----------
CREATE TABLE suppliers (
    supplier_id   BIGINT       NOT NULL AUTO_INCREMENT,
    supplier_code VARCHAR(50)  NOT NULL,
    supplier_name VARCHAR(255) NOT NULL,
    address       VARCHAR(500),
    phone         VARCHAR(50),
    email         VARCHAR(255),
    active_flag   TINYINT      NOT NULL DEFAULT 1,
    created_by    VARCHAR(100),
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(100),
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (supplier_id),
    CONSTRAINT uk_suppliers_code UNIQUE (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- MATERIALS ----------
CREATE TABLE materials (
    material_id   BIGINT        NOT NULL AUTO_INCREMENT,
    material_code VARCHAR(50)   NOT NULL,
    material_name VARCHAR(255)  NOT NULL,
    unit          VARCHAR(20)   NOT NULL,
    current_qty   DECIMAL(18,3) NOT NULL DEFAULT 0,
    minimum_qty   DECIMAL(18,3) NOT NULL DEFAULT 0,
    maximum_qty   DECIMAL(18,3) NOT NULL DEFAULT 0,
    average_cost  DECIMAL(18,2) NOT NULL DEFAULT 0,
    active_flag   TINYINT       NOT NULL DEFAULT 1,
    created_by    VARCHAR(100),
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    VARCHAR(100),
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (material_id),
    CONSTRAINT uk_materials_code UNIQUE (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- PRODUCTS ----------
CREATE TABLE products (
    product_id   BIGINT        NOT NULL AUTO_INCREMENT,
    product_code VARCHAR(50)   NOT NULL,
    product_name VARCHAR(255)  NOT NULL,
    sale_price   DECIMAL(18,2) NOT NULL DEFAULT 0,
    active_flag  TINYINT       NOT NULL DEFAULT 1,
    created_by   VARCHAR(100),
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(100),
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id),
    CONSTRAINT uk_products_code UNIQUE (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- RECIPES (BOM header) ----------
CREATE TABLE recipes (
    recipe_id    BIGINT   NOT NULL AUTO_INCREMENT,
    product_id   BIGINT   NOT NULL,
    version_no   INT      NOT NULL DEFAULT 1,
    active_flag  TINYINT  NOT NULL DEFAULT 1,
    created_by   VARCHAR(100),
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(100),
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (recipe_id),
    CONSTRAINT fk_recipes_product FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- RECIPE DETAILS (BOM lines) ----------
CREATE TABLE recipe_details (
    recipe_detail_id BIGINT        NOT NULL AUTO_INCREMENT,
    recipe_id        BIGINT        NOT NULL,
    material_id      BIGINT        NOT NULL,
    standard_qty     DECIMAL(18,3) NOT NULL DEFAULT 0,
    PRIMARY KEY (recipe_detail_id),
    CONSTRAINT fk_rd_recipe   FOREIGN KEY (recipe_id)   REFERENCES recipes (recipe_id) ON DELETE CASCADE,
    CONSTRAINT fk_rd_material FOREIGN KEY (material_id) REFERENCES materials (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- GOODS RECEIPT (header) ----------
CREATE TABLE goods_receipts (
    receipt_id   BIGINT        NOT NULL AUTO_INCREMENT,
    receipt_no   VARCHAR(30)   NOT NULL,
    receipt_date DATE          NOT NULL,
    supplier_id  BIGINT,
    note         VARCHAR(500),
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    created_by   VARCHAR(100),
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(100),
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (receipt_id),
    CONSTRAINT uk_gr_no UNIQUE (receipt_no),
    CONSTRAINT fk_gr_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- GOODS RECEIPT DETAILS ----------
CREATE TABLE goods_receipt_details (
    receipt_detail_id BIGINT        NOT NULL AUTO_INCREMENT,
    receipt_id        BIGINT        NOT NULL,
    material_id       BIGINT        NOT NULL,
    quantity          DECIMAL(18,3) NOT NULL DEFAULT 0,
    unit_price        DECIMAL(18,2) NOT NULL DEFAULT 0,
    amount            DECIMAL(18,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (receipt_detail_id),
    CONSTRAINT fk_grd_receipt  FOREIGN KEY (receipt_id)  REFERENCES goods_receipts (receipt_id) ON DELETE CASCADE,
    CONSTRAINT fk_grd_material FOREIGN KEY (material_id) REFERENCES materials (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- SALES (each line = product sold on a date) ----------
CREATE TABLE sales (
    sale_id     BIGINT        NOT NULL AUTO_INCREMENT,
    sale_date   DATE          NOT NULL,
    product_id  BIGINT        NOT NULL,
    quantity    DECIMAL(18,3) NOT NULL DEFAULT 0,
    batch_no    VARCHAR(30),
    created_by  VARCHAR(100),
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (sale_id),
    CONSTRAINT fk_sales_product FOREIGN KEY (product_id) REFERENCES products (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- INVENTORY TRANSACTIONS (every movement) ----------
CREATE TABLE inventory_transactions (
    txn_id       BIGINT        NOT NULL AUTO_INCREMENT,
    txn_date     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    txn_type     VARCHAR(20)   NOT NULL,  -- RECEIPT / ADJUSTMENT / SALE_CONSUMPTION / STOCK_COUNT
    reference_no VARCHAR(40),
    material_id  BIGINT        NOT NULL,
    quantity     DECIMAL(18,3) NOT NULL,  -- signed: + increase, - decrease
    before_qty   DECIMAL(18,3) NOT NULL,
    after_qty    DECIMAL(18,3) NOT NULL,
    unit_cost    DECIMAL(18,2) NOT NULL DEFAULT 0,
    note         VARCHAR(500),
    created_by   VARCHAR(100),
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (txn_id),
    CONSTRAINT fk_txn_material FOREIGN KEY (material_id) REFERENCES materials (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
