
# 🏫 Campus Sphere — Multi-Tenant Workflow Engine (UIT RGPV)

![Status](https://img.shields.io/badge/Status-Active_Development-success)
![Architecture](https://img.shields.io/badge/Architecture-Multi_Tenant_Microservices-purple)
![Kafka](https://img.shields.io/badge/Eventing-Kafka-orange)
![Realtime](https://img.shields.io/badge/Realtime-WebSocket-blue)
![Security](https://img.shields.io/badge/Security-JWT_RBAC-green)
![Cloud](https://img.shields.io/badge/Cloud-Ready-informational)

> **Domain:** Academic Workflow Automation Platform  
> **Architecture:** Multi-Tenant Event-Driven Microservices  
> **Purpose:** Enable multiple institutions to operate independent academic workflows on a shared distributed backend.

---

## 🚀 Overview

**Campus Sphere** is a distributed backend platform designed as a **multi-tenant workflow engine** for academic institutions.

Instead of building separate systems for each college, the platform allows multiple institutes to operate securely on a shared infrastructure while maintaining strict data isolation.

The system manages:

- User onboarding & authentication
- Department & organization workflows
- Approval chains
- Real-time notifications
- Tenant-scoped authorization

Each tenant (college/institution) operates logically independent while sharing the same infrastructure.

---

## 🧠 Core Architecture Idea

The platform follows two core backend principles:

### 1️⃣ Multi-Tenant Logical Isolation
A shared PostgreSQL database stores data for multiple institutions, but every request is scoped using a tenant identifier.

```

Request → Gateway → Tenant Context → Service → Tenant-Scoped Data Access

```

This ensures:
- No cross-organization data leaks
- Independent workflows per institution
- Cost-efficient infrastructure

---

### 2️⃣ Event-Driven Communication

Services do not directly depend on each other.

```

Service Action → Publish Domain Event → Kafka → Subscriber Reacts

```

Example:

```

Admin Approves User
↓
Auth Service publishes event
↓
Kafka Topic
↓
Notification Service
↓
WebSocket + Email alert

```

This removes tight coupling and allows the system to scale independently.

---

## 🏗 System Architecture

### Core Services

| Service | Responsibility |
|------|------|
| Service Registry | Dynamic service discovery |
| API Gateway | Centralized routing + tenant validation |
| Auth Service | Authentication, JWT, RBAC |
| Admin Service | Organization & department workflow |
| Notification Service | Real-time & transactional alerts |

---

## 🔔 Notification Pipeline

The platform contains a dedicated asynchronous notification system:

**Capabilities**

- Kafka event consumption
- WebSocket real-time push updates
- Email delivery via Brevo
- Priority based alerts
- Tenant-aware notifications

---

## 🔐 Security Model

The system implements layered security:

- Stateless JWT authentication
- Multi-Level Role Based Access Control
- Tenant-Scoped Authorization
- Gateway request validation
- Internal service protection

Each request carries both:

```

User Identity + Tenant Identity

```

This prevents cross-tenant data access.

---

## 🛠 Technology Stack

| Layer | Technology |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Microservices | Spring Cloud (Gateway + Eureka) |
| Messaging | Apache Kafka |
| Realtime | WebSocket (STOMP) |
| Security | JWT + RBAC |
| Database | PostgreSQL (Multi-Tenant) |
| Email | Brevo SMTP/API |
| Cache (Planned) | Redis |
| Deployment | Cloud Ready |

---

## 📦 Repository Structure

```

CampusSphere
├── ServiceRegistry
├── ApiGateway
├── AuthService
├── AdminService
└── NotificationService

```

---

## 📈 Development Status

The platform is currently in active development.

Upcoming modules:

- Redis caching layer
- Workflow engine extensions
- Analytics dashboards
- Retry & Dead Letter Queue
- Resume parsing & academic records module

---

## 👨‍💻 Author

**Prakhar Sakhare**  
Backend & Distributed Systems Developer

---

> This project is built to simulate real production SaaS architecture where multiple organizations share infrastructure while maintaining strict logical isolation.
