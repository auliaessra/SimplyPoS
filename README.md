# 🧴 Simply PoS — Business Management System

> A modern, multi-branch ERP & POS system built for perfume retail businesses.
> Powered by **Spring Boot 4.1**, **PostgreSQL**, **Thymeleaf**, and **Bootstrap 5**.

[![Spring Boot](https://img.shields.io/badge/Spring Boot-4.1.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/projects/jdk/25/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Production%20Ready-success.svg)]()

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Live Demo & Access](#-live-demo--access)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Configuration](#️-configuration)
- [Screenshots](#-screenshots)
- [Business Flows](#-business-flows)
- [Roadmap](#roadmap)

---

## 🌟 Overview

**Simply PoS** is a complete business management solution designed specifically for **perfume retail businesses**. It provides a unified platform for managing multiple branches, tracking inventory by volume (milliliters), handling purchasing and sales workflows, and generating comprehensive financial reports.

The system supports **multi-language** (Indonesian & English), **multi-branch operations**, **role-based access control**, **WhatsApp notifications**, **barcode/QR scanning**, and **dark mode** — all accessible through a modern, responsive web interface inspired by Microsoft Dynamics 365 Business Central.

### 🎯 Who Is It For?

- **Retail shop owners** looking to digitize operations
- **Multi-branch retailers** needing centralized inventory control
- **Small-to-medium enterprises** requiring POS + ERP functionality
- **Businesses needing accurate financial tracking** with double-entry accounting

---

## 🚀 Live Demo & Access

### 🌐 Demo URL
http://demo.essra.net


> **Note**: The demo is hosted on a staging server with sample data. It resets every 24 hours.
> For a fresh install, see the [Quick Start](#-quick-start) section.

### 👥 Demo Users

All demo accounts use the **same password** for convenience:

| Role                 | Username       | Password     | Access Scope                              |
| -------------------- | -------------- | ------------ | ----------------------------------------- |
| **SUPER_ADMIN**      | `superadmin`   | `admin123`   | Full access to all features & branches    |
| **ADMIN**            | `admin`        | `admin123`   | Manage own branch (central store)         |
| **ADMIN** (Branch 2) | `admin2`    | `admin123`   | Manage branch 2 (downtown)                |
| **CASHIER**          | `cashier`      | `admin123`   | POS & sales operations only               |
| **STAFF**            | `staff`        | `admin123`   | Purchase & inventory operations           |

### ⚠️ Demo Limitations

| Feature                | Demo              | Production       |
| ---------------------- | ----------------- | ---------------- |
| **Login**              | ✅ Enabled        | ✅ Enabled       |
| **Read operations**    | ✅ Enabled        | ✅ Enabled       |
| **Create/Update**      | ✅ Enabled        | ✅ Enabled       |
| **Delete**             | ⚠️ Auto-revert    | ✅ Enabled       |
| **File uploads**       | ⚠️ Disabled       | ✅ Enabled       |
| **WhatsApp API**       | ⚠️ Mocked         | ✅ Live          |
| **Email**              | ⚠️ Mocked         | ✅ Live          |
| **Data persistence**   | 🔄 Reset daily    | ✅ Permanent     |

### 🔗 Quick Access Links

- **Login Page**: https://demo.essra.net/login
- **Dashboard**: https://demo.essra.net/web/dashboard
- **POS Interface**: https://demo.essra.net/web/sales/pos
- **API Docs (Swagger)**: https://demo.essra.net/swagger-ui.html

---

## ✨ Key Features

### 🏢 Multi-Branch Management

- Support for **unlimited branches** (stores)
- **Store context switching** for SUPER_ADMIN
- Complete **data isolation** per branch (users, inventory, prices, transactions)
- Independent **stock levels** per branch
- **Cross-branch consolidated reports**

### 👥 Role-Based Access Control (RBAC)

Five distinct roles with **granular permissions**:

| Role            | Scope                                                     |
| --------------- | --------------------------------------------------------- |
| **SUPER_ADMIN** | Full system access, manage branches & users               |
| **ADMIN**       | Full access within own branch                             |
| **CASHIER**     | POS operations, view items/customers, process sales       |
| **STAFF**       | Purchase orders, invoices, vendor management              |
| **CUSTOMER**    | Self-service portal (future roadmap)                      |

**Menu visibility** automatically adapts based on role — user only sees features they can access.

### 🛒 Point of Sale (POS)

- **Touch-optimized** interface for tablets & mobile
- **Product search** by name, code, or **barcode scan**
- **Multi-payment methods**: Cash, QRIS, Credit (with customer tracking)
- **Real-time cart** with automatic subtotal & change calculation
- **Thermal receipt printer** integration (58mm/80mm)
- **Barcode/QR scanning** via device camera
- **Sticky cart bar** for mobile devices
- **Offline-ready** structure for future offline sync

### 📦 Inventory Management

- **Unit support**: ML, Liter, Botol, PCS, Box, Pack, KG, Gram
- **Unit conversion engine** (e.g., 1 Liter = 1000 ML)
- **Stock tracking per branch** in native unit
- **Minimum stock alerts**
- **Stock adjustment** via Item Journal (Opname, Damage, Loss, Found)
- **Movement history** via Item Ledger with running balance
- **Inventory valuation** report (current value by branch)

### 💰 Purchasing & Accounts Payable

- **Purchase Order (PO)** — Draft → Confirmed → Received workflow
- **Purchase Invoice (PI)** — Auto stock update + AP posting
- **Vendor Payment** — Partial/full payment tracking
- **Vendor Ledger** — Real-time AP balance per vendor
- **Aging AP report** — 5 aging buckets (Current, 1-30, 31-60, 61-90, 90+)

### 🧾 Sales & Accounts Receivable

- **Direct POS sales** — Cash & QRIS (immediate payment)
- **Credit sales** — With customer selection & AR tracking
- **Sales Order management** — List, view, void (with stock reversal)
- **Customer Payment** — Record partial/full AR payments
- **Customer Ledger** — Per-customer AR history with running balance
- **Aging AR report** — 5 aging buckets
- **Auto HPP posting** — COGS calculated per transaction

### 📊 Double-Entry Accounting

Complete **General Ledger** with automatic posting:

| Transaction         | Debit                           | Credit                    |
| ------------------- | ------------------------------- | ------------------------- |
| Purchase Invoice    | Persediaan (1301), PPN (1302)   | Hutang Usaha (2101)       |
| Vendor Payment      | Hutang Usaha (2101)             | Kas (1101) / Bank (1102)  |
| Sales (Cash/QRIS)   | Kas (1101) / Bank (1102)        | Penjualan (4101)          |
| Sales COGS          | HPP (5101)                      | Persediaan (1301)         |
| Sales (Credit)      | Piutang Usaha (1201)            | Penjualan (4101)          |
| Customer Payment    | Kas (1101) / Bank (1102)        | Piutang Usaha (1201)      |
| Item Journal (+)    | Persediaan (1301)               | Pendapatan Penyesuaian    |
| Item Journal (−)    | Beban Penyesuaian (5901)        | Persediaan (1301)         |

**Every journal entry is validated** — total debit must equal total credit.

### 📈 Financial Reports

- **Trial Balance** — Debit/credit balance check per account
- **Profit & Loss** — Revenue − Expense = Net Profit
- **Balance Sheet** — Assets = Liabilities + Equity (with current year profit)
- **General Ledger** — Detailed journal entries with filtering
- **Account filtering** — By branch, account, date range

### 📊 Reporting & Analytics

- **Interactive Dashboard** — 8 KPI cards + 4 Chart.js visualizations
  - Today/Month sales, profit, purchase
  - AR/AP outstanding, inventory value, low stock count
  - Sales trend (30d/12m), top products, purchase trend
- **Sales Report** — Filter by date range & branch
- **Purchase Report** — Top vendors, outstanding AP
- **Inventory Report** — Full valuation + low stock
- **Aging Reports** — AR & AP with 5-bucket aging

### 🌐 Multi-Language Support

- **Bahasa Indonesia** (default)
- **English**
- **User preference persistence** via cookie (365 days)
- **All UI elements translated** (labels, buttons, validation, alerts)
- **Number & date format** adapts to locale
- **Currency prefix** (`Rp` vs `IDR`)

### 🎨 Dark Mode

- **Toggle in topbar** (Sun/Moon icon)
- **3 modes**: Light / Dark / Auto (follows system)
- **No flash** — Set via inline script before CSS load
- **Persistent** via localStorage
- **All components supported** — cards, tables, forms, badges, modals, POS

### 📷 Barcode / QR Code Scanning

- **Camera-based scanning** via `html5-qrcode`
- **USB/Bluetooth wedge scanner** support (auto-detect)
- **Manual input** fallback
- **iOS/Android/Desktop** compatible
- **Integration points**:
  - Master Item — Fill barcode field / search item
  - POS — Add product to cart instantly

### 🔔 WhatsApp Notifications (Planned Integration)

Pre-configured for WA gateway integration:

- **Digital receipt** (e-struk) after sale
- **Low stock alert** to admin
- **Payment reminders** for due AP/AR
- **Configurable via Settings**

### ⚙️ Master Data Management

- **Chart of Accounts (CoA)** — Global, hierarchical
- **Items** — With barcode, unit, category
- **Units** — ML, Liter, Botol, PCS, etc.
- **Unit Conversions** — Flexible ratio configuration
- **Store Prices** — Buy/sell price per item per branch
- **Vendors** — Per branch
- **Customers** — Per branch
- **Stores/Branches** — Full CRUD with active toggle
- **Users** — Role assignment, password reset, activate/deactivate

### 🎨 Application Settings

- **Login page customization** — Logo, title, subtitle, background color, favicon
- **Sidebar branding** — Logo, brand name, brand text
- **Favicon** — Custom upload with validation
- **Multi-language default** — Set default language
- **Theme mode** — Light/Dark/Auto

### 🔒 Security Features

- **BCrypt password hashing** (strength 12)
- **JWT authentication** for API
- **Session-based** for web UI
- **Rate limiting** — Per-IP throttling (login: 5/min, public: 10/min)
- **Security headers** — HSTS, XSS protection, CSP, X-Frame-Options
- **CSRF protection** (configurable)
- **Audit logging** — All CRUD + status changes
- **Input sanitization** — XSS prevention
- **Role-based menu hiding** — Even if URL is known, `@PreAuthorize` blocks access

### 📱 Responsive Design

- **Desktop** (≥1025px) — Full 2-column layouts
- **Tablet** (769–1024px) — Adaptive layouts
- **Mobile** (≤768px) — Stacked layouts, drawer navigation
- **POS mobile** — Slide-in cart, sticky cart bar
- **Touch-optimized** — Large tap targets, gesture support

### 🌍 Additional Features

- **Multi-currency ready** (IDR default, extensible)
- **Timezone support** — Asia/Jakarta (configurable)
- **Auto-generated document numbers** — PO0001, PI0001, SO0001, VPAY0001, CPAY0001, IJ0001
- **Print-friendly** pages (Ctrl+P optimized)
- **CSV/PDF export** (planned)

---

## 🛠 Technology Stack

### Backend

| Component            | Version      | Purpose                              |
| -------------------- | ------------ | ------------------------------------ |
| **Spring Boot**      | 3.3.5        | Core application framework           |
| **Java**             | 21 (LTS)     | Runtime environment                  |
| **Spring Security**  | 6.x          | Authentication & authorization       |
| **Spring Data JPA**  | 3.x          | ORM & repository abstraction         |
| **Hibernate**        | 6.x          | JPA implementation                   |
| **PostgreSQL**       | 16+          | Primary relational database          |
| **Flyway**           | 10.x         | Database migration & versioning      |
| **JJWT**             | 0.12.6       | JWT token generation                 |
| **Bucket4j**         | 8.10.1       | Rate limiting                        |
| **Caffeine**         | Latest       | In-memory cache                      |
| **Lombok**           | Latest       | Boilerplate reduction                |

### Frontend

| Component            | Version      | Purpose                              |
| -------------------- | ------------ | ------------------------------------ |
| **Thymeleaf**        | 3.1          | Server-side template engine          |
| **Thymeleaf Layout** | Latest       | Layout dialect                       |
| **Bootstrap**        | 5.3.3        | CSS framework                        |
| **Bootstrap Icons**  | 1.11.3       | Icon library                         |
| **Chart.js**         | 4.4.1        | Data visualization                   |
| **html5-qrcode**     | 2.3.8        | Barcode/QR scanning                  |
| **Custom CSS**       | -            | BC365-inspired design system         |

### DevOps & Infrastructure

| Component            | Version      | Purpose                              |
| -------------------- | ------------ | ------------------------------------ |
| **Docker**           | Latest       | Containerization                     |
| **Docker Compose**   | v2           | Multi-container orchestration        |
| **Nginx**            | 1.27         | Reverse proxy & SSL termination      |
| **Let's Encrypt**    | Latest       | SSL certificates                     |
| **Prometheus**       | Latest       | Metrics collection                   |
| **Grafana**          | Latest       | Metrics visualization                |
| **GitHub Actions**   | -            | CI/CD pipeline                       |

---

## 🏗 Architecture

### System Architecture
┌─────────────────────────────────────────────────────────────┐
│ Clients │
│ (Web Browser, Tablet, Mobile, Barcode Scanner) │
└────────────────────────┬────────────────────────────────────┘
│ HTTPS
▼
┌─────────────────────────────────────────────────────────────┐
│ Nginx (Reverse Proxy) │
│ - SSL/TLS termination │
│ - Rate limiting │
│ - Static asset caching │
│ - Security headers │
└────────────────────────┬────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│ Spring Boot Application │
│ │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ │
│ │ Web UI │ │ REST API │ │ Scheduler │ │
│ │ (Thymeleaf) │ │ (/api/v1/*) │ │ (Cron) │ │
│ └─────────────┘ └─────────────┘ └─────────────┘ │
│ │
│ ┌────────────────────────────────────────────────┐ │
│ │ Business Logic (Services) │ │
│ │ - Master Data - Purchasing - Sales │ │
│ │ - Inventory - Accounting - Reporting │ │
│ └────────────────────────────────────────────────┘ │
│ │
│ ┌────────────────────────────────────────────────┐ │
│ │ Data Access (JPA Repositories) │ │
│ └────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│ PostgreSQL Database │
│ - 30+ tables (users, items, orders, ledgers, etc.) │
│ - Flyway-managed schema │
│ - Multi-branch data isolation │
└─────────────────────────────────────────────────────────────┘

### Module Structure
com.simplypos
├── config/ → Security, i18n, MVC configs
├── core/
│ ├── security/ → JWT, RBAC, rate limiting
│ └── monitoring/ → Health indicators
├── entities/ → Shared entities (User, Store)
├── modules/
│ ├── auth/ → Login, logout, user management
│ ├── master/ → Master data (CoA, Items, Vendors, etc.)
│ │ ├── coa/
│ │ ├── item/
│ │ ├── unit/
│ │ ├── store/
│ │ ├── vendor/
│ │ ├── customer/
│ │ └── storeprice/
│ ├── purchase/ → PO, PI, Vendor Payment
│ ├── sales/ → POS, Sales Order, Customer Payment
│ ├── inventory/ → Item Journal, Item Ledger, Customer Ledger
│ ├── finance/ → General Ledger, Reports
│ ├── reporting/ → Dashboard, Sales/Purchase/Inventory Reports
│ ├── setting/ → App settings (logo, favicon, theme)
│ └── audit/ → Audit log
├── web/ → Controllers (dashboard, profile, settings)
└── SimplyPosApplication.java

## ⚙️ Configuration
### Environment Variables
| Variable                  |	Required    |   Default                                                 |   Description |
| DB_URL	                |   ✅          |   PostgreSQL JDBC URL                                     |               |
| DB_USER	                |   ✅          |   postgres	Database username                              |               |
| DB_PASSWORD	            |   ✅	       |   root	Database password                                  |               |
| JWT_SECRET	            |   ✅	       |   (dev only)	JWT signing key (min 256 bit / 32 chars)   |               |
| JWT_EXPIRATION_MS	        |   ❌	       |   28800000	JWT expiry (8 hours)                           |               |
| SERVER_PORT	            |   ❌	       |   8080	HTTP port                                          |               |
| SPRING_PROFILES_ACTIVE	|   ❌	       |   dev	dev or prod                                        |               |
| LOG_FILE	                |   ❌	       |   ./logs/app.log	Log file location                      |               |
| UPLOADS_DIR	            |   ❌	       |   ./uploads	File uploads directory                     |               |
| TZ	                    |   ❌	       |   Asia/Jakarta	Timezone                                   |               |

### Profiles

    dev (default) — SQL logging, Thymeleaf cache off, verbose logs

    prod — SQL logging off, cache on, minimal logs, external config required

### Application Settings (Runtime)

All branding & UI settings are configurable via Settings menu (SUPER_ADMIN only):

    Login page: logo, title, subtitle, background color, favicon

    Sidebar: brand name, logo, brand text

    General: app name, tagline, footer

### Settings are stored in the app_settings table and cached for performance.
📸 Screenshots

    Screenshots coming soon. In the meantime, visit the live demo to explore the interface.

### Planned Screenshots

    🏠 Dashboard — KPI cards + interactive charts

    🛒 POS Interface — Product catalog + cart (desktop & mobile)

    📦 Item Management — Master data with barcode scanning

    💰 Purchase Invoice — Full PI workflow with GL preview

    📊 Financial Reports — Trial balance, P&L, Balance Sheet

    🌙 Dark Mode — Side-by-side light/dark comparison

    📱 Mobile View — Responsive POS with sticky cart bar

## 🔄 Business Flows
### Purchase Flow
1. Purchase Order (PO)
   ├─ Create PO → DRAFT
   ├─ Confirm PO → CONFIRMED
   └─ Receives goods

2. Purchase Invoice (PI)
   ├─ Create PI from PO (or directly)
   ├─ Post PI → Stock +, AP +, GL posted
   └─ Status: POSTED

3. Vendor Payment
   ├─ Select outstanding PI
   ├─ Record payment (Cash/Transfer/Giro)
   ├─ Post → AP −, GL posted
   └─ PI Status: PARTIAL_PAID or PAID

### Sales Flow
1. POS Sale (Cash/QRIS)
   ├─ Add products to cart
   ├─ Select payment method
   ├─ Complete → Stock −, GL posted
   └─ Status: COMPLETED

2. Credit Sale
   ├─ Select customer
   ├─ Complete → Stock −, AR +, GL posted
   └─ Status: COMPLETED

3. Customer Payment (Credit Only)
   ├─ Select outstanding SO
   ├─ Record payment (Cash/Transfer)
   ├─ Post → AR −, GL posted
   └─ SO Status updated

4. Void Sale
   ├─ Reverse stock, AR, GL
   ├─ Status: VOID
   └─ Audit log recorded

Stock Adjustment Flow
1. Item Journal
   ├─ Reason: Opname, Damage, Loss, Found
   ├─ Qty: +/- ML
   ├─ Post → Item Ledger + GL posted
   └─ Status: POSTED

## 🗺 Roadmap
### ✅ Completed (v1.0.0)
    ☑ Multi-branch management
    ☑ RBAC with 5 roles
    ☑ POS with barcode scanning
    ☑ Purchase & Sales workflows
    ☑ Double-entry accounting
    ☑ Financial reports (Trial Balance, P&L, Balance Sheet)
    ☑ Interactive dashboard with charts
    ☑ Multi-language (ID/EN)
    ☑ Dark mode
    ☑ Application settings
    ☑ Docker deployment
    ☑ SSL with Let's Encrypt
    ☑ Monitoring (Prometheus + Grafana)

### 🚧 In Progress (v1.1.0)
    □ WhatsApp notification integration (Fonnte/WATI)
    □ Excel export for all reports
    □ PDF invoice generation with company logo
    □ Customer portal (self-service)

### 📅 Planned (v1.2.0+)
    □ Android POS app (Kotlin + Jetpack Compose)
    □ Offline mode for POS (PWA)
    □ Loyalty program (points, discounts)
    □ Employee attendance system
    □ Multi-currency support
    □ Advanced analytics (trend, forecast)
    □ REST API v2 (GraphQL)
    □ Mobile app for admin (iOS/Android)