<div align="center">

# 🧮 Math Engine PRO

**Enterprise-Grade Computer Algebra System (CAS) & Data Visualization Protocol**

[![Vercel Deployment](https://img.shields.io/badge/Edge_Delivery-Vercel-black?style=for-the-badge&logo=vercel)](https://math-engine-three.vercel.app/)
[![Render Backend](https://img.shields.io/badge/Container_Host-Render-46E3B7?style=for-the-badge&logo=render&logoColor=white)](#)
[![Java Spring Boot](https://img.shields.io/badge/Core_API-Java_21_|_Spring-6DB33F?style=for-the-badge&logo=spring)](https://spring.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](https://opensource.org/licenses/MIT)

*A decoupled, high-performance web application engineered to parse complex mathematical inputs, execute symbolic calculus via a custom Abstract Syntax Tree (AST), and render interactive, low-latency 2D visualizations.*

[**Live Production Environment**](https://math-engine-three.vercel.app/) • [**Issue Tracker**](#) • [**System Architecture**](#)

</div>

---

## 📸 System Telemetry & Interface

<div align="center">
  <img src="https://github.com/user-attachments/assets/d54e8851-289e-42cd-bd5c-13790970ffc6" alt="Command Center Interface & Digital Notebook" width="800" />
  <p><i>Command Center Interface & Digital Notebook</i></p>
</div>

### Sub-System Capabilities
<div align="center">
  <img src="https://github.com/user-attachments/assets/67872fc4-9ca8-4499-be8d-84780b3171d6" />
  <img src="https://github.com/user-attachments/assets/1c05b4ac-1fea-4987-a7d3-e21ddf2920a8" alt="Symbolic Calculus Evaluation" width="400" />
</div>

---

## 🚀 Architectural Blueprint

Math Engine Pro implements a strict separation of concerns, utilizing a secure, asynchronous API bridge between an edge-delivered lightweight client and a containerized, heavy-duty computational engine.

### 🛡️ Security Protocols & Access Control
* **Gateway Authentication:** Custom API key headers enforced at the ingress layer to mitigate automated volumetric traffic.
* **CORS Vaulting:** Cross-Origin Resource Sharing (CORS) is strictly configured via Spring Security filters, rejecting all computational requests originating outside the verified Vercel production domain.

### 🧠 Core Computational Engine (Backend)
* **Symbolic Calculus Parsing:** Natively evaluates definite and indefinite integrals without external dependencies, ensuring data sovereignty.
* **Abstract Syntax Tree (AST):** Constructs and traverses custom syntax trees to guarantee strict order-of-operations compliance and efficient algebraic routing.
* **Algorithmic Polynomial Extraction:** Programmatically detects quadratic structures to compute the discriminant ($\Delta$) and root properties.
* **Containerized Deployment:** Leverages multi-stage Docker builds to optimize the Java 21 runtime environment for scalable cloud infrastructure.

### ⚡ Client-Side Edge Delivery (Frontend)
* **Zero-Dependency Architecture:** Engineered using Vanilla JavaScript (ES6+), HTML5, and CSS3 to eliminate framework overhead and maximize rendering velocity.
* **Mathematical Typesetting:** Integrates KaTeX and MathJax for textbook-quality, real-time LaTeX rendering.
* **Interactive Telemetry:** Utilizes Chart.js for dynamic, localized 2D coordinate plotting on the Cartesian plane.

---

## 🛠️ Technology Stack & Infrastructure

| Sub-System | Applied Technologies |
| :--- | :--- |
| **Client Interface** | Vanilla JS, HTML5, CSS3, KaTeX, MathJax, Chart.js |
| **Core API** | Java 21, Spring Boot, Maven |
| **DevOps & CI/CD** | Docker, Git, Render (PaaS), Vercel (Edge CDN) |
| **Transport Protocol** | RESTful JSON |

---

## 💻 Local Environment Initialization

To replicate the production environment locally for development or auditing purposes, execute the following protocol:

### System Prerequisites
* **Java Development Kit (JDK) 21**
* **Apache Maven**
* **Docker Engine** (Optional, for localized container testing)
* **VS Code** (Equipped with Live Server)

### Phase 1: Backend Initialization
Navigate to the backend directory, compile the application, and initialize the Spring Boot container:

```bash
cd math-parsing-engine
mvn clean package -DskipTests
java -jar target/app.jar
```
The CAS Engine will bind to and listen on http://localhost:8080.

### Phase 2: Frontend Configuration
1. Navigate to the Frontend directory within your IDE.
2. Modify config.js to route traffic to the local loopback address:

```bash
const API_BASE_URL = "http://localhost:8080/api/v1";
```

3. Initialize index.html via VS Code Live Server.




📜 Licensing & Usage

Distributed under the MIT License. Reference LICENSE for comprehensive legal documentation.
