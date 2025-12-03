# 🎓 IT Department Portal (UIT RGPV)

![Status](https://img.shields.io/badge/Status-In_Development-yellow)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-green)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-blueviolet)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)

> **Current Phase:** Phase 1 (Infrastructure & Scaffolding)  
> **Client:** IT Department, University Institute of Technology (UIT), RGPV  
> **Version:** 2.0 (Development Build)

## 🚧 Project Status & Overview

The **IT Department Portal** is an upcoming centralized platform for managing student and faculty data, research showcases, and automated reporting.

**Currently, the project is in the initial development phase.** We have established the **Microservices Architecture Skeleton**, allowing for the independent development and deployment of future modules. The foundational infrastructure (Service Discovery, API Gateway, and Containerization) is operational.

---

## ✅ Current Implementation Details (Infrastructure)

The following architectural components are currently set up and running:

* **[x] Service Registry (Eureka Server):** Centralized discovery for all microservices.
* **[x] API Gateway:** Unified entry point for routing requests to specific services.
* **[x] Docker Orchestration:** `docker-compose.yml` is configured to spin up the service skeleton.
* **[x] Service Scaffolding:** Spring Boot projects created for:
    * `user-service`
    * `resume-parser-service`
    * `export-service`
    * `phd-service`

---

## 🗺️ Roadmap: Planned Features (From SRS)

We are following a phased development lifecycle based on SRS v2.0.

### 🔜 Phase 1: Core Functionality (Next Steps)
- [ ] **Database Integration:** Connecting PostgreSQL (Users/Transactional) and MongoDB (Profiles).
- [ ] **Security Layer:** Implementing Spring Security with JWT Authentication in the `user-service`.
- [ ] **Basic Profile CRUD:** APIs for creating Student and Faculty profiles.
- [ ] **Frontend Integration:** Connecting the React.js basics to the API Gateway.

### 📅 Phase 2: Advanced Features
- [ ] **Resume Parser Logic:** Implementing the logic to extract JSON from PDF/DOCX uploads.
- [ ] **Approval Workflow:** Building the Student $\rightarrow$ Faculty $\rightarrow$ Admin verification chain.
- [ ] **Reporting Service:** Implementing Apache POI (Excel) and iText (PDF) generation logic.

### 📅 Phase 3: Scaling & Public Modules
- [ ] **PhD Scholar Showcase:** Dynamic content delivery for research publications.
- [ ] **Redis Caching:** Optimizing response times for public directories.
- [ ] **Notification System:** Real-time alerts for profile status updates.

---

## 🏗 System Architecture

The project utilizes a **Microservices Architecture** to ensure scalability.



[Image of microservices architecture diagram]


**Services Breakdown:**
1.  **User/Profile Service:** (Skeleton Ready) - Will handle Auth & Data.
2.  **Resume Parser Service:** (Skeleton Ready) - Will handle NLP extraction.
3.  **Export Service:** (Skeleton Ready) - Will handle File generation.
4.  **PhD Service:** (Skeleton Ready) - Will handle Research data.
5.  **Homepage Service:** (Planned) - Public content management.

---

## 🛠 Tech Stack

| Domain | Technology |
| :--- | :--- |
| **Backend Framework** | Spring Boot (Java 17) |
| **Microservices** | Spring Cloud Gateway, Eureka |
| **Databases** | PostgreSQL (Relational) & MongoDB (Flexible) |
| **Frontend** | React.js (Planned) |
| **DevOps** | Docker, Docker Compose |

---

## 🚀 Getting Started (Running the Skeleton)

Since the project is currently in the infrastructure phase, running the project will spin up the empty services and the discovery server.

### Prerequisites
* Docker & Docker Compose
* Java 17+
* Maven

### Steps
1.  **Clone the repository**
    ```bash
    git clone [https://github.com/your-username/it-dept-portal.git](https://github.com/your-username/it-dept-portal.git)
    cd it-dept-portal
    ```

2.  **Build the Services**
    Navigate to the root and build the JARs (ensure you have a root `pom.xml` or build individually):
    ```bash
    mvn clean package -DskipTests
    ```

3.  **Start Infrastructure**
    ```bash
    docker-compose up --build
    ```

4.  **Verify Connectivity**
    * **Eureka Dashboard:** `http://localhost:8761` (Check if services are registered)
    * **API Gateway:** `http://localhost:8080`

---

## 👥 Authors

* **Prakhar Sakhare**
* **Gavesh Gatham**
* **Mentor:** Dr. Mahesh Pawar

---
*Disclaimer: This repository is currently under active construction. Features listed in the Roadmap are subject to development as per the SRS requirements.*
