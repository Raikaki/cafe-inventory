# CAFE INVENTORY MANAGEMENT SYSTEM

## AI Development Specification (SQL First + Spring Boot + Docker)

---

# 1. PROJECT OVERVIEW

## Objective

Build a production-ready Cafe Inventory Management System that allows:

* Inventory management
* Material consumption tracking
* Recipe/BOM management
* Stock in/out transactions
* Cost calculation
* Sales import from POS
* Dashboard and reporting
* Automatic inventory deduction based on sales quantity

The system must be designed using **SQL First Architecture**.

---

# 2. TECHNOLOGY STACK

## Backend

* Java 21
* Spring Boot 3.x
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate
* Flyway Migration
* Lombok
* MapStruct
* Swagger OpenAPI

## Database

* MySQL 8

## Frontend

* Thymeleaf
* Bootstrap 5
* Chart.js

## Infrastructure

* Docker
* Docker Compose

## Deployment

* Railway
* Render
* Koyeb

---

# 3. ARCHITECTURE REQUIREMENT

The project MUST follow:

```text
Database
    ↓
Schema SQL
    ↓
Migration Script
    ↓
Entity
    ↓
Repository
    ↓
Service
    ↓
Controller
    ↓
UI
```

Never design Entity first.

Database design must be completed before coding.

---

# 4. BUSINESS FLOW

```text
Purchase Material
       ↓
Inventory Increase
       ↓

Create Product Recipe
       ↓

Record Sales
       ↓

Recipe Calculation
       ↓

Material Consumption
       ↓

Inventory Decrease
       ↓

Inventory Reports
```

---

# 5. USER MANAGEMENT

## Role

### ADMIN

Permissions:

* Full Access
* User Management
* System Configuration

### MANAGER

Permissions:

* Inventory Management
* Recipe Management
* Reporting

### STAFF

Permissions:

* Goods Receipt
* Stock Count
* Sales Import

### VIEWER

Permissions:

* Read Only

---

# 6. MATERIAL MANAGEMENT

## Purpose

Manage all ingredients and consumables.

Examples:

```text
Coffee Bean
Coffee Powder
Condensed Milk
Fresh Milk
Sugar
Peach Tea
Bubble
Ice
Plastic Cup
Straw
Lid
```

---

## Table: materials

| Column        | Type          |
| ------------- | ------------- |
| material_id   | BIGINT        |
| material_code | VARCHAR(50)   |
| material_name | VARCHAR(255)  |
| unit          | VARCHAR(20)   |
| current_qty   | DECIMAL(18,3) |
| minimum_qty   | DECIMAL(18,3) |
| maximum_qty   | DECIMAL(18,3) |
| average_cost  | DECIMAL(18,2) |
| active_flag   | TINYINT       |
| created_at    | DATETIME      |
| updated_at    | DATETIME      |

---

# 7. PRODUCT MANAGEMENT

Products sold to customers.

Examples:

```text
Coffee Black
Coffee Milk
Bac Xiu
Peach Tea
Milk Tea
Matcha Latte
```

---

## Table: products

| Column       | Type          |
| ------------ | ------------- |
| product_id   | BIGINT        |
| product_code | VARCHAR(50)   |
| product_name | VARCHAR(255)  |
| sale_price   | DECIMAL(18,2) |
| active_flag  | TINYINT       |

---

# 8. RECIPE (BOM) MANAGEMENT

This is the core module.

Every product must have a recipe.

---

## Example

Coffee Milk

```text
Coffee Powder = 20g
Condensed Milk = 30ml
Ice = 150g
Plastic Cup = 1
Straw = 1
Lid = 1
```

---

## Table: recipes

| Column      | Type    |
| ----------- | ------- |
| recipe_id   | BIGINT  |
| product_id  | BIGINT  |
| version_no  | INT     |
| active_flag | TINYINT |

---

## Table: recipe_details

| Column           | Type          |
| ---------------- | ------------- |
| recipe_detail_id | BIGINT        |
| recipe_id        | BIGINT        |
| material_id      | BIGINT        |
| standard_qty     | DECIMAL(18,3) |

---

# 9. SUPPLIER MANAGEMENT

## Table: suppliers

| Column        | Type         |
| ------------- | ------------ |
| supplier_id   | BIGINT       |
| supplier_code | VARCHAR(50)  |
| supplier_name | VARCHAR(255) |
| address       | VARCHAR(500) |
| phone         | VARCHAR(50)  |
| email         | VARCHAR(255) |

---

# 10. GOODS RECEIPT

Inventory Increase

---

## Header

Table:

```sql
goods_receipts
```

Document Number:

```text
GR2026000001
```

---

## Detail

Table:

```sql
goods_receipt_details
```

Fields:

```text
Material
Quantity
Unit Price
Amount
```

---

# 11. STOCK ADJUSTMENT

Manual inventory adjustment.

Reasons:

```text
Broken
Expired
Lost
Promotion
Waste
```

---

## Adjustment Types

```text
Increase
Decrease
```

---

# 12. SALES IMPORT

The system receives sales quantity.

---

## Method 1

Manual Entry

Example:

```text
Coffee Milk = 50
Peach Tea = 20
```

---

## Method 2

Excel Import

Format:

```text
Sale Date
Product Code
Quantity
```

Example:

```text
2026-01-01
CF001
50
```

---

# 13. AUTOMATIC MATERIAL CONSUMPTION

Most important business logic.

---

## Example

Coffee Milk Recipe

```text
Coffee = 20g
Milk = 30ml
Ice = 150g
Cup = 1
```

Sales:

```text
Coffee Milk = 100
```

System calculates:

```text
Coffee = 2000g
Milk = 3000ml
Ice = 15000g
Cup = 100
```

---

## Inventory Update

```text
Before Inventory

Coffee = 10kg

After Inventory

Coffee = 8kg
```

Inventory must be deducted automatically.

---

# 14. INVENTORY TRANSACTION

Every movement must be recorded.

---

## Table: inventory_transactions

Fields:

```text
Transaction Date
Transaction Type
Reference No
Material
Quantity
Before Qty
After Qty
```

---

## Transaction Types

```text
RECEIPT
ADJUSTMENT
SALE_CONSUMPTION
STOCK_COUNT
```

---

# 15. STOCK COUNT

Physical inventory checking.

---

## Process

System Qty:

```text
Coffee = 5000g
```

Actual Qty:

```text
Coffee = 4700g
```

Difference:

```text
-300g
```

Generate Adjustment Transaction automatically.

---

# 16. COST CALCULATION

System must calculate product cost automatically.

---

## Example

Coffee Milk

```text
Coffee = 2500
Milk = 1500
Cup = 800
Straw = 200
```

Total Cost:

```text
5000 VND
```

---

## Formula

```text
Cost = Sum(Material Cost × Recipe Qty)
```

---

# 17. DASHBOARD

## KPI

Show:

```text
Total Products
Total Materials
Current Inventory Value
Today's Sales
Today's Consumption
Low Stock Materials
```

---

## Charts

### Monthly Sales

### Monthly Consumption

### Top Selling Products

### Low Stock Materials

---

# 18. REPORTS

## Inventory Balance

```text
Opening
Receipt
Consumption
Adjustment
Closing
```

---

## Inventory Card

Transaction History

---

## Material Consumption Report

By date range.

---

## Product Cost Report

Show cost per product.

---

## Low Stock Report

Material below minimum quantity.

---

# 19. API REQUIREMENTS

REST API for all modules.

Example:

```http
GET /api/materials

POST /api/materials

PUT /api/materials/{id}

DELETE /api/materials/{id}
```

---

# 20. DATABASE REQUIREMENTS

Generate:

```text
V1__schema.sql
V2__index.sql
V3__views.sql
V4__procedures.sql
V5__seed_data.sql
```

---

## Views

```sql
vw_inventory_balance

vw_inventory_transaction

vw_low_stock

vw_product_cost
```

---

## Procedures

```sql
sp_goods_receipt

sp_stock_adjustment

sp_sales_consumption

sp_stock_count
```

---

# 21. SECURITY

Authentication:

```text
JWT
```

Password:

```text
BCrypt
```

Authorization:

```text
Role Based Access Control
```

---

# 22. AUDIT FIELDS

All tables must contain:

```sql
created_by
created_at
updated_by
updated_at
```

---

# 23. LOGGING

Use:

```java
@Slf4j
```

Log:

```text
Login
Receipt
Adjustment
Sales Import
Recipe Change
```

---

# 24. FILE IMPORT

Support:

```text
Excel (.xlsx)
CSV
```

Libraries:

```text
Apache POI
OpenCSV
```

---

# 25. TESTING

Generate:

```text
Unit Test
Integration Test
Repository Test
Service Test
```

Coverage:

```text
Minimum 80%
```

---

# 26. DOCKER REQUIREMENT

Generate:

```text
Dockerfile
docker-compose.yml
.env.example
```

System must run using:

```bash
docker compose up -d
```

Without manual database setup.

---

# 27. PROJECT STRUCTURE

```text
src/main/java

controller
service
repository
entity
dto
mapper
security
config
exception
util

src/main/resources

db/migration
templates
static
```

---

# 28. DELIVERABLES

Claude must generate:

* Complete Source Code
* Database Scripts
* Flyway Migration
* Dockerfile
* Docker Compose
* Swagger Documentation
* ERD Diagram
* README
* Deployment Guide

---

# 29. SUCCESS CRITERIA

The generated application must:

✓ Start successfully

✓ Create database automatically

✓ Support JWT Login

✓ Manage materials

✓ Manage recipes

✓ Import sales

✓ Auto calculate consumption

✓ Auto deduct inventory

✓ Dashboard available

✓ Reports available

✓ Docker deploy successfully

✓ Production-ready architecture

---

# FINAL INSTRUCTION TO CLAUDE

Generate the entire project source code.

Do not generate examples only.

Generate all backend, frontend, database scripts, Docker files, Swagger documentation, Flyway migration, unit tests, README, ERD and deployment guide.

The application must run successfully after executing:

```bash
docker compose up -d
```

without requiring any manual database configuration.

```
```
