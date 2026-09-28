# DonateHub – Old Clothes and Book Donation Drive Tracker

**DonateHub** is a clean, modular Spring Boot web application designed for organizing, managing, and tracking donation drives for old clothes and books. The system connects donors, donation drives, donated inventory, and recipient charity organizations, with real-time tracking of collected items, distributed stock, and remaining quantities.

The application is completely self-contained within a single Spring Boot project, serving a lightweight, responsive **HTML, CSS, and Vanilla JavaScript** frontend directly without requiring any separate frontend servers, Node.js, or build frameworks.

---

## 🎯 Purpose & Problem Solved

Managing community donation drives often suffers from:
- Poor inventory tracking (unknown quantity of clothes and books collected vs. distributed).
- Disconnected records between donors, donation drives, and beneficiary recipient organizations.
- Stock discrepancies resulting in items being over-distributed beyond available physical inventory.

**DonateHub** solves these challenges by providing:
1. **End-to-End Tracking**: Links donors, donation campaigns, item inventories, and recipient charity partners.
2. **Real-time Stock Balances**: Automatically computes Total Collected, Distributed Units, and Remaining In-Stock items.
3. **Distribution Safeguards**: Validates item conditions, categories (`Clothes`, `Books`), quantity > 0, and strictly prevents over-distribution.
4. **Single-Service Simplicity**: Runs both REST APIs and the user-facing web dashboard from a unified Spring Boot application.

---

## 🛠 Technologies Used

- **Backend**:
  - Java 17+
  - Spring Boot 4.x / Spring Web MVC
  - Spring Data JPA / Hibernate ORM
  - MySQL Database (`mysql-connector-j`)
  - Project Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- **Frontend**:
  - Semantic HTML5
  - Modern, responsive CSS3 (Clean, professional card and table layout)
  - Pure Vanilla JavaScript (Native ES6 `fetch()` API with relative URLs)
  - Zero external frontend frameworks (No React, No Vite, No Node.js, No npm)
- **Testing & Build**:
  - Apache Maven (via included `mvnw` wrapper)
  - JUnit 5 & Spring MockMvc

---

## 🏗 Architecture

The backend strictly follows a layered architecture without DTO bloat or custom exception layers:

```text
  Browser / Vanilla JS (fetch)
               ↓
           Controller
               ↓
            Service
               ↓
          Repository
               ↓
            Entity
               ↓
             MySQL
```

### Key Architectural Highlights:
- **Unified Spring Boot Deployment**: HTML templates and static CSS/JS assets reside in `src/main/resources/` and are served directly by Spring Boot.
- **Relative API Calls**: JavaScript communicates with backend REST controllers using relative paths (`/donors`, `/recipients`, `/drives`, `/donated-items`), avoiding hardcoded ports or cross-origin restrictions.
- **Zero DTOs**: Entity classes are used directly in Controller request and response payloads, ensuring straightforward data flow.
- **Module-by-Entity Package Layout**: Each domain entity forms its own cohesive module under `com.donatehub`:
  - `com.donatehub.Donor` (entity, Repository, Service, Controller)
  - `com.donatehub.Recipient` (entity, Repository, Service, Controller)
  - `com.donatehub.Drive` (entity, Repository, Service, Controller)
  - `com.donatehub.DonatedItem` (entity, Repository, Service, Controller)
- **Lombok Integration**: Eliminates repetitive boilerplate with `@Data`, `@NoArgsConstructor`, and `@AllArgsConstructor`.
- **Integrated Distribution Tracking**: Remaining stock is computed dynamically from `quantity` and `distributedQuantity` without complex relational overhead.

---

## 📂 Project Structure

```text
DonateHub/
├── pom.xml                                   # Project dependencies & build configuration
├── .gitignore                                # Excludes build artifacts, secrets, and IDE files
├── README.md                                 # Complete documentation
├── mvnw                                      # Maven wrapper (Linux/macOS)
├── mvnw.cmd                                  # Maven wrapper (Windows)
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── donatehub/
    │   │           ├── DonateHubApplication.java       # Spring Boot entry point
    │   │           │
    │   │           ├── config/
    │   │           │   ├── CorsConfig.java             # CORS & WebMvc configuration
    │   │           │   └── PageController.java         # Spring MVC controller serving HTML pages
    │   │           │
    │   │           ├── Donor/
    │   │           │   ├── Controller/DonorController.java
    │   │           │   ├── Service/DonorService.java
    │   │           │   ├── Repository/DonorRepository.java
    │   │           │   └── entity/Donor.java
    │   │           │
    │   │           ├── Recipient/
    │   │           │   ├── Controller/RecipientController.java
    │   │           │   ├── Service/RecipientService.java
    │   │           │   ├── Repository/RecipientRepository.java
    │   │           │   └── entity/Recipient.java
    │   │           │
    │   │           ├── Drive/
    │   │           │   ├── Controller/DriveController.java
    │   │           │   ├── Service/DriveService.java
    │   │           │   ├── Repository/DriveRepository.java
    │   │           │   └── entity/Drive.java
    │   │           │
    │   │           └── DonatedItem/
    │   │               ├── Controller/DonatedItemController.java
    │   │               ├── Service/DonatedItemService.java
    │   │               ├── Repository/DonatedItemRepository.java
    │   │               └── entity/DonatedItem.java
    │   │
    │   └── resources/
    │       ├── application.properties                  # Database configuration (environment variables)
    │       │
    │       ├── templates/                              # User-facing HTML pages
    │       │   ├── index.html                          # Community Dashboard
    │       │   ├── donors.html                         # Donor management
    │       │   ├── recipients.html                     # Recipient organizations management
    │       │   ├── drives.html                         # Donation drives & campaign summaries
    │       │   └── donated-items.html                  # Items catalog & distribution
    │       │
    │       └── static/                                 # Pure CSS & Vanilla JavaScript
    │           ├── css/
    │           │   └── style.css                       # Responsive design system
    │           └── js/
    │               ├── common.js                       # Universal fetch wrapper & modal helpers
    │               ├── dashboard.js                    # Metric counters & recent activity
    │               ├── donors.js                       # Donor CRUD handlers
    │               ├── recipients.js                   # Recipient CRUD handlers
    │               ├── drives.js                       # Drive CRUD & summary modals
    │               └── donated-items.js                # Item CRUD & distribution handlers
    │
    └── test/
        └── java/
            └── com/
                └── donatehub/
                    ├── DonateHubApplicationTests.java  # Application context verification
                    ├── DonateHubControllerTests.java   # REST Controller MockMvc tests
                    ├── DonateHubServiceTests.java      # Service business logic & validation tests
                    └── PageAndStaticResourceTests.java # Page route & static resource resolution tests
```

---

## 🗄 Entity Schema & Database Tables

### 1. Donor (`donor`)
| Field | Column Name | Type | Description |
|---|---|---|---|
| `id` | `donor_id` | `Long` | Primary Key (`IDENTITY`) |
| `name` | `name` | `String` | Donor's full name |
| `email` | `email` | `String` | Email address |
| `phone` | `phone` | `String` | Phone number |
| `address` | `address` | `String` | Mailing address |

### 2. Recipient (`recipient`)
| Field | Column Name | Type | Description |
|---|---|---|---|
| `id` | `recipient_id` | `Long` | Primary Key (`IDENTITY`) |
| `name` | `name` | `String` | Recipient center / name |
| `organizationName` | `organization_name` | `String` | NGO or organization name |
| `contactPerson` | `contact_person` | `String` | Primary contact person |
| `phone` | `phone` | `String` | Phone number |
| `address` | `address` | `String` | Facility address |

### 3. Drive (`drive`)
| Field | Column Name | Type | Description |
|---|---|---|---|
| `id` | `drive_id` | `Long` | Primary Key (`IDENTITY`) |
| `name` | `name` | `String` | Campaign name |
| `startDate` | `start_date` | `LocalDate` | Campaign start date |
| `endDate` | `end_date` | `LocalDate` | Campaign end date |
| `description` | `description` | `String` | Description of goals |
| `recipientId` | `recipient_id` | `Long` | Foreign key referencing `recipient.recipient_id` |

### 4. DonatedItem (`donated_item`)
| Field | Column Name | Type | Description |
|---|---|---|---|
| `id` | `donated_item_id` | `Long` | Primary Key (`IDENTITY`) |
| `name` | `name` | `String` | Item description (e.g. Winter Coats) |
| `category` | `category` | `String` | Category: `Clothes` or `Books` |
| `condition` | `item_condition` | `String` | Condition: `New`, `Good`, or `Worn` |
| `quantity` | `quantity` | `int` | Total donated quantity (> 0) |
| `distributedQuantity` | `distributed_quantity` | `int` | Distributed quantity (starts at 0) |
| `donorId` | `donor_id` | `Long` | Foreign key referencing `donor.donor_id` |
| `driveId` | `drive_id` | `Long` | Foreign key referencing `drive.drive_id` |

---

## 📋 Business Rules & Validation

1. **Quantity Positive Constraint**: Every donated item must have `quantity > 0`.
2. **Category Restriction**: Allowed categories are strictly `Clothes` and `Books`.
3. **Condition Classification**: Allowed conditions are strictly `New`, `Good`, and `Worn`.
4. **Foreign Key Integrity**: Each item must link to an existing `donorId` and `driveId`. Each drive must link to an existing `recipientId`.
5. **Timeline Consistency**: `startDate` and `endDate` are mandatory for drives; `endDate` cannot be before `startDate`.
6. **Distribution Inventory Boundary**: A distribution request for quantity $Q$ cannot exceed the available stock:
   $$\text{Remaining Stock} = \text{quantity} - \text{distributedQuantity}$$
   If $Q > \text{Remaining Stock}$, the transaction is rejected.

---

## 🌐 REST API Endpoints

### Donors
- `POST   /donors` — Create a new donor
- `GET    /donors` — List all donors
- `GET    /donors/{id}` — Get donor by ID
- `PUT    /donors/{id}` — Update donor details
- `DELETE /donors/{id}` — Delete a donor

### Recipients
- `POST   /recipients` — Create a new recipient partner
- `GET    /recipients` — List all recipient partners
- `GET    /recipients/{id}` — Get recipient by ID
- `PUT    /recipients/{id}` — Update recipient details
- `DELETE /recipients/{id}` — Delete a recipient

### Drives
- `POST   /drives` — Create a new donation campaign
- `GET    /drives` — List all donation campaigns
- `GET    /drives/{id}` — Get drive by ID
- `PUT    /drives/{id}` — Update drive details
- `DELETE /drives/{id}` — Delete a drive
- `GET    /drives/{id}/items` — List all donated items linked to this drive
- `GET    /drives/{id}/summary` — Returns real-time stock counts:
  ```json
  {
    "driveId": 1,
    "driveName": "Winter Clothing Drive",
    "totalQuantity": 50,
    "distributedQuantity": 18,
    "remainingQuantity": 32
  }
  ```

### Donated Items
- `POST   /donated-items` — Record a new donated item
- `GET    /donated-items` — List all donated items
- `GET    /donated-items/{id}` — Get donated item by ID
- `PUT    /donated-items/{id}` — Update donated item details
- `PUT    /donated-items/{id}/distribute?quantity=X` — Distribute X units from remaining inventory
- `DELETE /donated-items/{id}` — Delete a donated item

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java 17+** (OpenJDK 17 or higher)
- **MySQL 8.0+**

### 2. Configure Database Credentials
Database connection parameters in `src/main/resources/application.properties` use environment variables with safe fallbacks (no hardcoded passwords committed):

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/DonateHub?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

You can pass your credentials as environment variables when launching:

```bash
# Windows PowerShell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_mysql_password"

# Linux / macOS
export DB_USERNAME="root"
export DB_PASSWORD="your_mysql_password"
```

### 3. Run the Application
From the project root:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Once started, open your browser:
- **Dashboard**: `http://localhost:8080/`
- **Donors**: `http://localhost:8080/donors.html`
- **Recipients**: `http://localhost:8080/recipients.html`
- **Drives**: `http://localhost:8080/drives.html`
- **Donated Items**: `http://localhost:8080/donated-items.html`

---

## 🧪 Automated Testing

To run the entire suite of 15 automated unit and integration tests:

```bash
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

### Test Suite Breakdown:
1. **`DonateHubApplicationTests`**: Verifies Spring context initialization.
2. **`DonateHubControllerTests`**: Tests MockMvc REST endpoints for all 4 controllers (CRUD and distribution).
3. **`DonateHubServiceTests`**: Tests business validations, inventory boundaries, and summary calculations.
4. **`PageAndStaticResourceTests`**: Validates root routing, HTML page serving, and CSS/JS asset availability.
