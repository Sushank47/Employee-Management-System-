# Architecture Integration & Progress Report (Readme2.md)

This document provides a detailed breakdown of the development actions completed, the purpose of the newly installed/created modules, and the step-by-step connection flow linking the Frontend, Backend, and MongoDB Database.

---

## 1. 🛠️ What has been Done & Installed

### Backend Relocation & Refactoring
- **Codebase Relocation**: Moved the entire project package hierarchy from `com.company.hrms` to `com.sushank.ems`. All package headers, imports, and references in configuration files were updated.
- **Security Updates**: Modified the security configuration (`SecurityConfig.java`) to permit public access to API docs and Swagger UI paths, resolving CORS or authorization conflicts when reading OpenAPI definitions.
- **Database Seeder (`DatabaseSeeder.java`)**: Implemented an automated seeder that runs on application startup. It inspects the database and inserts a default admin account, departments, employees, leave requests, projects, and payroll logs if they are missing.

### Frontend Client Setup
- **Vite & React 19 Client**: Created the frontend directory using Vite with React 19, enabling extremely fast hot module reloading.
- **Bloom Aesthetic Styling (`index.css`)**: Built a custom, Tailwind-compatible vanilla CSS utility framework inside `index.css`. This styling utilizes strict HSL grayscale parameters, modern Google Fonts (Poppins & Source Serif 4), looping muted mp4 background video playback, and liquid glass card borders through the CSS mask-composite technique.
- **Ecosystem Integration (`App.jsx` & `api.js`)**: Developed state management to connect React with the Spring Boot API, rendering interactive analytics dashboard stats, employee lists, clocking interfaces, and leave request logs.

---

## 2. 🔌 How Frontend, Backend, and Database Connect

The system functions through a three-tier architecture. Here is how they communicate:

```
┌──────────────────────────────────────┐
│       Frontend (Vite / React 19)     │  ◄─── Runs on port 5173
└──────────────────┬───────────────────┘
                   │
                   │ (HTTP Fetch Requests / JWT Auth Headers)
                   ▼ [Proxied by Vite Server]
┌──────────────────────────────────────┐
│       Backend (Spring Boot API)      │  ◄─── Runs on port 8080
└──────────────────┬───────────────────┘
                   │
                   │ (MongoDB Native Protocol / Java Driver)
                   ▼
┌──────────────────────────────────────┐
│       Database (MongoDB Local)       │  ◄─── Runs on port 27017
└──────────────────────────────────────┘
```

### A. Connecting Frontend to Backend
1. **The API Proxy**: In development, Vite runs on port `5173` and Spring Boot runs on port `8080`. To avoid Cross-Origin Resource Sharing (CORS) security blocks in the browser, Vite is configured in [`vite.config.js`](file:///Users/sushank's%20macbook/Desktop/Employee%20Management%20System/client/vite.config.js) to capture any outgoing client request starting with `/api` and transparently forward it to `http://localhost:8080`.
2. **REST Requests**: The frontend uses native browser `fetch` APIs in [`api.js`](file:///Users/sushank's%20macbook/Desktop/Employee%20Management%20System/client/src/services/api.js) to query endpoints.
3. **Security Access**: Upon a successful login (`POST /api/auth/login`), the backend returns a JSON Web Token (JWT). The frontend stores this token in browser `localStorage`. Every subsequent request automatically attaches this token in the header:
   `Authorization: Bearer <JWT_TOKEN>`

### B. Connecting Backend to Database
1. **Driver Integration**: The Spring Boot backend includes the `spring-boot-starter-data-mongodb` dependency. This imports the official Mongo Java Driver.
2. **Database Coordinates**: Coordinates are defined in [`server/src/main/resources/application.yml`](file:///Users/sushank's%20macbook/Desktop/Employee%20Management%20System/server/src/main/resources/application.yml):
   `uri: mongodb://localhost:27017/employee_db`
   Spring Boot automatically boots up a `MongoClient` pointing to this connection string when launched.
3. **Object Mapping (ODM)**: Entities in Java (e.g., `Employee.java`, `User.java`) are marked with the `@Document` annotation. Spring Data MongoDB automatically manages the conversion between Java objects and BSON documents stored in the database.

---

## 3. 🎯 Purpose of Key Components

- **[`DatabaseSeeder.java`](file:///Users/sushank's%20macbook/Desktop/Employee%20Management%20System/server/src/main/java/com/sushank/ems/config/DatabaseSeeder.java)**: Ensures that a database connection is not only validated on startup, but is populated with sample data so that the frontend charts and tables render immediately without manual bootstrapping.
- **[`JwtAuthenticationFilter.java`](file:///Users/sushank's%20macbook/Desktop/Employee%20Management%20System/server/src/main/java/com/sushank/ems/security/JwtAuthenticationFilter.java)**: Intercepts all incoming HTTP requests to check for the JWT token, extracts the authenticated username/roles, and registers them in Spring's SecurityContext.
- **[`api.js`](file:///Users/sushank's%20macbook/Desktop/Employee%20Management%20System/client/src/services/api.js)**: Acts as the central communication controller for the frontend, consolidating all CRUD methods, parameters, and headers.
- **[`index.css`](file:///Users/sushank's%20macbook/Desktop/Employee%20Management%20System/client/src/index.css)**: Implements the premium, professional liquid glass aesthetic using custom backdrop filters, gradient borders, and micro-hover animations.
