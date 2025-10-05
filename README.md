# WanderInn

Production-ready backend for a hotel reservation system built with **Spring Boot 3** and **PostgreSQL/JPA**. It powers **search, booking, payments, and admin**. Dynamic pricing uses **Strategy + Decorator**; hot paths are accelerated with **Caffeine** caching (measured **~58%**).

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)

---

## Features
- Search & booking across **10k+ listings**
- **JWT** auth (access/refresh) with role-based authorization
- **Dynamic pricing** via Strategy + Decorator (occupancy, seasonality/holiday, surge, promos)
- **Stripe Checkout** + signature-verified, webhooks
- **Caffeine caching** (per-instance) + min-price precompute → **~58% faster search**
- OpenAPI/Swagger docs, global exception mapping

---

## Architecture (at a glance)
- **Controllers** → REST endpoints, validation, global error mapping  
- **Services** → search, booking, pricing, payments  
- **Repositories (JPA)** → PostgreSQL with paging & selective locking  
- **Pricing Engine** → composable modifiers (Decorator) wrapped in strategies  
- **Caching (Caffeine)** → `inventoryByRoom`, `inventorySearch` with targeted evictions on writes  
- **Webhooks** → deduplicated by `event_id` for idempotency

```mermaid
flowchart LR
  Client -->|REST| Controller --> Service --> Repo --> PostgreSQL
  Service -->|Pricing| PricingEngine
  Service -->|Cache| Caffeine[(Caffeine)]
  Stripe((Stripe)) -->|Webhooks| Controller

erDiagram
  HOTEL ||--o{ ROOM : has
  ROOM  ||--o{ INVENTORY : has
  HOTEL {
    UUID id
    string name
    string city
  }
  ROOM {
    UUID id
    int  totalCount
    money basePrice
  }
  INVENTORY {
    UUID id
    date date
    int  totalCount
    int  bookedCount
    int  reservedCount
    money price
    bool closed
  }
