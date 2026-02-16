
# 🎓 IT Department Portal — Event Driven Microservices Platform (UIT RGPV)

![Status](https://img.shields.io/badge/Status-Active_Development-success)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green)
![Architecture](https://img.shields.io/badge/Architecture-Event_Driven_Microservices-purple)
![Kafka](https://img.shields.io/badge/Eventing-Kafka-orange)
![WebSocket](https://img.shields.io/badge/Realtime-WebSocket-blue)
![Cloud](https://img.shields.io/badge/Cloud-Ready-informational)

> **Client:** IT Department — University Institute of Technology, RGPV  
> **Architecture Version:** v2 (Distributed System)  
> **Goal:** Production-style academic workflow automation platform

---

## 🚀 Overview

The **IT Department Portal** is a distributed backend system designed to automate academic workflows such as:

- Student & faculty onboarding
- Department & college management
- Approval workflows
- Secure authentication & authorization
- Real-time notifications

Unlike traditional monolithic college portals, this platform is built as a **cloud-native event-driven microservices architecture**.

Services communicate:
- **Synchronously → API Gateway (REST)**
- **Asynchronously → Kafka Events**

This enables independent deployment, scaling, and failure isolation.

---

## 🧠 Core Concept (Event Driven Flow)

Instead of tightly coupled service-to-service calls:

```

Service → Publish Event → Kafka → Consumer Reacts

```

Example:

```

User Approved
↓
Auth Service emits event
↓
Kafka Topic
↓
Notification Service
↓
WebSocket + Email

```

---

## 🏗️ System Architecture

### Core Services

| Service | Responsibility |
|------|------|
| **Service Registry (Eureka)** | Dynamic service discovery |
| **API Gateway** | Routing + authentication validation |
| **Auth Service** | Login, signup, JWT, OTP, roles |
| **Admin Service** | College & department management |
| **Notification Service** | Event consumer & real-time notifications |

---

### Event Communication

```

Auth/Admin Services
↓
Kafka Cloud
↓
Notification Service
↙           ↘
WebSocket     Email (Brevo)

```

---

## 🔔 Notification Engine

The platform includes a dedicated **Notification Microservice** capable of:

- Consuming Kafka domain events
- Persisting notifications in PostgreSQL
- Real-time WebSocket push alerts
- Transactional email sending
- Priority based notifications
- Extensible for SMS / Push notifications

---

## 🛠 Technology Stack

| Domain | Technology |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Microservices | Spring Cloud Gateway + Eureka |
| Security | Spring Security + JWT |
| Messaging | Kafka (Cloud Hosted) |
| Realtime | WebSocket (STOMP) |
| Email | Brevo API |
| Database | PostgreSQL (Neon Cloud) |
| Architecture | Event-Driven + AOP Events |
| Deployment | Railway / Render Ready |

---

## 🔐 Security Design

- Stateless JWT authentication
- Gateway-level validation
- Role based authorization
- Internal service protection headers
- No direct service exposure

---

## 📦 Repository Structure

```

IT_Department_Portal
├── ServiceRegistry
├── ApiGateway
├── AuthService
├── AdminService
└── NotificationService

````

---

## 🧪 Running Locally

### Prerequisites
- Java 17
- Maven
- PostgreSQL Database
- Kafka Cloud Credentials

### Clone

```bash
git clone https://github.com/<your-username>/IT_Department_Portal.git
cd IT_Department_Portal
````

### Run Services (Order)

```
1. ServiceRegistry
2. ApiGateway
3. AuthService
4. AdminService
5. NotificationService
```

---

## 📈 Future Enhancements

* Resume parser service
* Redis caching layer
* Analytics dashboard
* User notification preferences
* Retry & Dead Letter Queue
* Frontend integration (React)

---

## 👨‍💻 Author

**Prakhar Sakhare**
B.Tech IT — UIT RGPV
Backend & Microservices Developer

---

> This project is built using real industry architecture patterns to simulate a production-grade distributed backend system rather than a simple CRUD application.

````

---

After pasting, commit:

```bash
git add README.md
git commit -m "docs: update project readme with architecture and tech stack"
git push
````

