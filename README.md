# Smart Employee Management & Workforce Analytics System

An enterprise-grade Employee Management System (EMS) and workforce analytics platform built with a high-performance **Java 17 / Spring Boot 3** backend, a **MongoDB** database, and a stunning **React 19 / Vite** frontend using custom grayscale liquid glassmorphism styling.

---

## 🚀 Active Ports & URLs

- **Frontend Application**: [http://localhost:5173](http://localhost:5173)
- **Backend API Server**: [http://localhost:8080](http://localhost:8080)
- **Swagger OpenAPI Documentation**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) *(Publicly accessible; use your JWT token to authorize requests)*

---

## 🔑 Testing Credentials

The database is pre-seeded on startup with a default administrator account and sample workforce metrics. Use these credentials to sign in and test the system:

- **Admin Email**: `admin@ems.com`
- **Admin Password**: `admin123`
- **Assigned Employee ID**: `EMP999`
- **Role**: `ROLE_ADMIN`

*Note: The database seeder also generates 3 departments, 6 employees, 4 leave requests, 2 projects, and 2 payroll runs to immediately populate the workforce analytics dashboard.*

---

## 🛠️ Technology Stack & Dependencies

### Backend (`/server`)
Built as a Maven project under Java 17 and Spring Boot 3.

- **Core Framework**: Spring Boot 3.3.0
- **Database**: MongoDB 8 (running locally at `mongodb://localhost:27017` on database `employee_db`)
- **Data Access**: Spring Data MongoDB
- **Security**: Spring Security & JWT Token Authentication (`jjwt-api`, `jjwt-impl`, `jjwt-jackson` version 0.12.5)
- **API Documentation**: Springdoc OpenAPI WebMVC UI 2.5.0
- **Utilities**: Lombok, Jakarta Validation, JUnit 5, Mockito
- **Java Compiler**: Release 17

### Frontend (`/client`)
Built as a modern React application utilizing a lightweight, custom utility-class system matching Tailwind semantics, avoiding bulky framework downloads.

- **View Library**: React 19.2.6 & React DOM 19.2.6
- **Routing**: React Router DOM 7.18.0
- **Build Tool**: Vite 8.0.12 (proxies `/api` requests to the backend at `http://localhost:8080`)
- **Icons**: Lucide React 1.21.0
- **Typography**: Poppins & Source Serif 4 (loaded via Google Fonts)
- **Styling**: Vanilla CSS (Grayscale HSL variables, backdrop blur filters, and fluid glassmorphism container effects) over a looping video background.

---

## 📋 System Architecture

The application adheres to an industry-standard layered architecture:
```
Client (React App) 
       │
       ▼ (REST API / JWT Auth)
Controller Layer (Spring Boot RestControllers)
       │
       ▼
Service Layer (Business Logic & Transactions)
       │
       ▼
Repository Layer (Spring Data MongoDB Repositories)
       │
       ▼
Database Layer (Local MongoDB 8 instance)
```

---

## ⚙️ How to Run the Project

### 1. Database Prerequisite
Ensure MongoDB is running locally on port `27017`.
```bash
mongosh --eval "db.adminCommand('ping')"
```

### 2. Run the Backend Server
Navigate to the `server` directory and launch the application:
```bash
cd server
export JAVA_HOME="/opt/homebrew/Cellar/openjdk@17/17.0.15/libexec/openjdk.jdk/Contents/Home" # Set JDK 17
mvn spring-boot:run
```

### 3. Run the Frontend Client
Navigate to the `client` directory, install dependencies, and run the Vite dev server:
```bash
cd client
npm install
npm run dev
```
