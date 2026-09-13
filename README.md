# InterCollege 🎓
> **"Discover. Participate. Connect."**  
> A modern intercollegiate event discovery and management web platform built with Java, Spring Boot, and modern web standards.

---

## 📌 Project Overview

**InterCollege** solves a major problem faced by college students: discovering competitions, hackathons, cultural festivals, workshops, and sports tournaments hosted by colleges around them.

The platform provides two dedicated role experiences:
1. **Student**: Discover events happening nearby, filter by category and distance, receive personalized recommendations based on interests, and get timely status alerts.
2. **Event Coordinator**: Publish, manage, edit, and monitor events for their own institution.

---

## ⚙️ Technology Stack

- **Backend**: Java 17+ / Spring Boot 3.3.4 (Spring MVC, REST APIs, Session Authentication)
- **Frontend**: HTML5, CSS3, Modern JavaScript (ES6+), HTML5 Canvas
- **Design System**: Dark Green & Mint Theme (`#091413`, `#285A48`, `#408A71`, `#B0E4CC`) with smooth glassmorphism and subtle constellation animation
- **Data Persistence**: **Java File Handling with JSON files** (`data/users.json`, `data/events.json`, `data/notifications.json`)
- **No Database**: Specifically designed for computer science students who have not yet studied relational or NoSQL database systems.

---

## 🚀 How to Run the Application

### Prerequisites
- **Java**: JDK 17 or higher installed (Tested and verified with Oracle JDK 26).
- **Port**: Port `8080` available.

### Quick Start (Windows)
You can launch the application with a single command from PowerShell or Command Prompt:

```powershell
cd C:\Users\sreen\.gemini\antigravity\scratch\intercollege
.\run.ps1
```
*(Or simply double-click `run.bat` in Windows File Explorer)*

### Alternative Start (Maven CLI)
If Maven is installed in your system PATH:
```bash
mvn spring-boot:run
```
Or with the included portable Maven:
```powershell
& 'C:\Users\sreen\.gemini\antigravity\scratch\apache-maven-3.9.9\bin\mvn.cmd' spring-boot:run
```

### Access in Browser
Once started, open your web browser and navigate to:
```
http://localhost:8080
```

---

## 🔑 Demo Accounts & Sample Credentials

The application auto-seeds **14+ realistic events across 5 colleges** and ready-to-test user accounts:

| Role | Username | Password | Institution | Specialization / Location |
| :--- | :--- | :--- | :--- | :--- |
| **Student** | `sreenanda` | `student123` | CET | Thiruvananthapuram (Tech, Makeathon, Workshop) |
| **Coordinator** | `arjun_cet` | `coord123` | College of Engineering Trivandrum (CET) | Thiruvananthapuram |
| **Coordinator** | `sneha_mec` | `coord123` | Model Engineering College (MEC) | Kochi |
| **Coordinator** | `rahul_mbcet` | `coord123` | Mar Baselios College of Eng & Tech | Thiruvananthapuram |
| **Coordinator** | `ananya_gec` | `coord123` | Government Engineering College (GEC) | Thrissur |
| **Coordinator** | `karthik_rset` | `coord123` | Rajagiri School of Eng & Tech (RSET) | Kochi |

> [!TIP]
> On the login screen, you can click the **1-Click Demo Login** buttons to instantly sign in without typing!

---

## 🌟 Key Features & Architectural Highlights

### 1. JSON File Persistence (No Database Required)
- Application data is persisted under the `data/` folder:
  - `data/users.json`: Hashed student and coordinator accounts.
  - `data/events.json`: Detailed event listings with timestamps, categories, and coordinates.
  - `data/notifications.json`: Smart notification logs and read states.
- Uses Jackson `ObjectMapper` configured with `JavaTimeModule` for `LocalDate` and `LocalTime`.
- Implements `ReentrantReadWriteLock` to guarantee safe concurrent reads and atomic file writes.

### 2. Location-Based Event Discovery (Haversine Formula)
- Browser Geolocation API allows students to click `[Use My Location]`.
- Calculates great-circle distance in kilometers using the spherical trigonometry Haversine formula in `GeoUtil.java`.
- Manual search also works independently without GPS: students can search cities like "Thiruvananthapuram", "Kochi", "Kozhikode", "Thrissur", etc.

### 3. Dynamic Urgency & Status System
Event statuses are dynamically computed in real-time from `LocalDate.now()` and `LocalTime.now()`:
- 🔴 **Happening Today**: Scheduled for today.
- 🟡 **Happening Soon**: Within 1 to 3 days.
- 🟢 **Upcoming**: Later this week or month.
- 🔵 **Ongoing**: Currently in progress between start and end time.
- ⚪ **Completed**: Date and time has passed.

### 4. Strict Coordinator Authorization
- When a coordinator creates an event, the system locks the host institution to their registered college.
- On edit and delete operations, `EventService` verifies that the coordinator's college matches the event's college. Unauthorized attempts return a `403 Forbidden` error.

### 5. External Registration Redirection
- Clicking `[ REGISTER NOW ]` validates the official URL and redirects to the external registration portal in a new browser tab.
- In accordance with privacy and operational guidelines, registration details are never stored within this platform.

### 6. Resetting Sample Data
To restore the sample data back to default:
- Send a POST request to `http://localhost:8080/api/dev/reset-data`
- Or simply delete the `data/` directory and restart the application.

---

## 📂 Project Structure

```
intercollege/
├── pom.xml                                      # Maven build configuration
├── run.bat                                      # Quick-start script for Windows CMD
├── run.ps1                                      # Quick-start script for PowerShell
├── README.md                                    # Project documentation
│
├── data/                                        # JSON File Persistence (auto-created)
│   ├── users.json
│   ├── events.json
│   └── notifications.json
│
└── src/
    └── main/
        ├── java/com/intercollege/
        │   ├── InterCollegeApplication.java     # Spring Boot Entrypoint
        │   ├── controller/                      # REST API Controllers
        │   │   ├── AuthController.java
        │   │   ├── EventController.java
        │   │   ├── UserController.java
        │   │   ├── NotificationController.java
        │   │   ├── DevController.java
        │   │   └── GlobalExceptionHandler.java
        │   ├── service/                         # Business & Security Logic
        │   │   ├── UserService.java
        │   │   ├── EventService.java
        │   │   ├── NotificationService.java
        │   │   └── DataInitializationService.java
        │   ├── model/                           # Domain Entities & Enums
        │   │   ├── User.java
        │   │   ├── Event.java
        │   │   ├── Notification.java
        │   │   ├── Role.java
        │   │   ├── EventCategory.java
        │   │   └── EventStatus.java
        │   ├── repository/                      # File Persistence Layer
        │   │   ├── JsonFileManager.java
        │   │   ├── UserRepository.java
        │   │   ├── EventRepository.java
        │   │   └── NotificationRepository.java
        │   ├── dto/                             # Data Transfer Objects
        │   │   ├── ApiResponse.java
        │   │   └── AuthRequests.java
        │   └── util/                            # Algorithms & Utilities
        │       ├── GeoUtil.java                 # Haversine Distance Calculator
        │       └── PasswordUtil.java            # Cryptographic Password Hasher
        │
        └── resources/
            ├── application.properties           # Spring Boot Configuration
            └── static/                          # Modern Frontend
                ├── index.html                   # Semantic SPA Layout
                ├── css/
                │   └── styles.css               # Design System (#091413 / #285A48 / #408A71 / #B0E4CC)
                └── js/
                    ├── background-canvas.js     # Animated Constellation Mesh
                    └── app.js                   # Client REST Controller & Geo Router
```
