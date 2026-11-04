# Faculty of Technology Academic Management System (FOT-AMS)
### ICT2132 Object Oriented Programming Practicum
### University of Ruhuna — Group 17

---

## Project Overview

**FOT-AMS** is a full-featured desktop academic management system built with Java Swing, JDBC, and MySQL (XAMPP). It supports four distinct user roles — **Admin**, **Lecturer**, **Technical Officer**, and **Undergraduate** — each with tailored dashboards and capabilities.

### Group Members

| Member | Student ID | Module Responsibility |
|---|---|---|
| H.R.H. Nethsara | TG/2024/2111 | Member 01 — User & Admin Management |
| Sahan Rashmika | TG/2024/2102 | Member 02 — Course Management |
| Anuda Pasandul | TG/2024/2101 | Member 03 — Marks & Grade Management |
| M.D.R.L. Nonis | TG/2024/2064 | Member 04 — Attendance, Medical & Communication |

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17+ |
| GUI Framework | Java Swing (Nimbus L&F) |
| Database | MySQL 8.x via XAMPP |
| Connectivity | JDBC (mysql-connector-j 8.3.0) |
| Build Tool | Maven 3.x (bundled with IntelliJ IDEA) |
| IDE | IntelliJ IDEA 2024+ |

---

## Prerequisites

Before running the application, ensure the following are installed and running:

1. **IntelliJ IDEA** (2024.1 or newer, Community or Ultimate)
2. **JDK 17 or higher** (Temurin 21 recommended — bundled with IntelliJ)
3. **XAMPP** with **MySQL service started**

---

## Setup Instructions

### Step 1 — Clone / Open the Project in IntelliJ IDEA

1. Open **IntelliJ IDEA**.
2. Choose **File → Open** and navigate to the project root folder (`New folder/`).
3. IntelliJ will detect the `pom.xml` and automatically import it as a Maven project.
4. Wait for Maven to download dependencies (requires internet access on first run).

### Step 2 — Start XAMPP MySQL

1. Open **XAMPP Control Panel**.
2. Click **Start** next to **MySQL**.
3. Verify the MySQL port shows `3306`.

### Step 3 — Import the Database

#### Option A: phpMyAdmin (Recommended for beginners)

1. Open your browser and go to: `http://localhost/phpmyadmin`
2. Click the **Import** tab in the left panel (or top menu).
3. Click **Choose File** and select:
   ```
   database/group17_db.sql
   ```
4. Scroll down and click **Import**.
5. After success, you will see the database `faculty_academic_management` in the left panel.

#### Option B: MySQL Command Line

```bash
"C:\xampp\mysql\bin\mysql.exe" -u root -p < "database\group17_db.sql"
```

> Leave the password blank (press Enter) unless you have set a MySQL root password.

### Step 4 — Configure the Database Connection (Optional)

The application reads its connection settings from:
```
src/main/resources/config.properties
```

Default settings (XAMPP defaults — no changes needed for a fresh XAMPP install):
```properties
db.host=localhost
db.port=3307
db.database=faculty_academic_management
db.username=root
db.password=
db.useSSL=false
db.serverTimezone=UTC
db.allowPublicKeyRetrieval=true
```

If your XAMPP MySQL has a root password, add it to `db.password=`.

### Step 5 — Build and Run

#### In IntelliJ IDEA:
1. Open the **Maven** panel (View → Tool Windows → Maven).
2. Run **Lifecycle → clean → compile** to verify the build.
3. Open `src/main/java/TG2111/Main.java`.
4. Right-click → **Run 'Main.main()'**.

#### Via PowerShell (project root):
```powershell
mvn clean compile
mvn package
```

---

## Default Login Credentials

| Role | Username | Password |
|---|---|---|
| **Administrator** | `admin` | `admin123` |
| **Lecturer** | `lec_kamal` | `lec123` |
| **Technical Officer** | `to_saman` | `to123` |
| **Undergraduate** | `tg20242001` | `student123` |

> **Security Note:** All passwords are stored as SHA-256 hashes in the database. Never store plain-text passwords in production.

### Creating a New Administrator Account (Secure Method)

Run this SQL in phpMyAdmin after computing the SHA-256 hash of your desired password:

```sql
-- Example: creating admin with password "MySecurePass123"
-- Compute hash using: echo -n "MySecurePass123" | sha256sum
INSERT INTO users (username, password_hash, role_id, full_name, email, department_id, designation, status)
VALUES (
    'new_admin',
    'SHA256_HASH_HERE',
    1,
    'New Admin Full Name',
    'newadmin@fot.ruh.ac.lk',
    1,
    'Administrator',
    'ACTIVE'
);
```

Or use the **Admin Dashboard → User Management → Add User** screen with the Admin role selected.

---

## Project Structure

```
src/main/java/
├── TG2111/  # Member 01: users/admin, shared infrastructure, and application entry point
├── TG2102/  # Member 02: course management
├── TG2101/  # Member 03: marks and grades
└── TG2064/  # Member 04: attendance, medical, notices, and timetables

src/main/resources/
└── config.properties                  # DB connection settings

database/
└── group17_db.sql                     # Standalone DB setup script
```

Java files are placed directly inside their member folder (no nested package folders). Their existing `com.fot.ams...` package declarations are preserved; Maven compiles all four source folders. Import `database/group17_db.sql` once to create the MySQL database and sample accounts.

---

## Module Breakdown

### Member 01 — User & Admin Management (H.R.H. Nethsara, TG/2024/2111)

**Primary files:**
- `model/User.java`, `model/Admin.java`, `model/Role.java`, `model/Department.java`
- `dao/UserDAO.java`, `dao/RoleDAO.java`, `dao/DepartmentDAO.java`
- `service/AuthenticationService.java`, `service/UserService.java`
- `ui/LoginFrame.java`, `ui/admin/AdminDashboard.java`, `ui/admin/UserManagementPanel.java`
- `util/PasswordUtil.java`, `util/SessionManager.java`, `util/ValidationUtil.java`

**Key features implemented:**
- Login screen with SHA-256 password verification
- Admin dashboard with summary statistics
- User CRUD (Add/View/Update/Delete) with role-specific fields
- Role-based access control: each role sees a different dashboard
- Status filtering (ACTIVE/INACTIVE users)
- Password change support

### Member 02 — Course Management (Sahan Rashmika, TG/2024/2102)

**Primary files:**
- `model/Course.java`, `model/CourseMaterial.java`, `model/CourseEnrollment.java`
- `dao/CourseDAO.java`, `dao/CourseMaterialDAO.java`, `dao/CourseEnrollmentDAO.java`
- `service/CourseService.java`
- `ui/admin/CourseManagementPanel.java`, `ui/lecturer/CourseMaterialPanel.java`
- `ui/undergraduate/CourseViewPanel.java`

**Key features implemented:**
- Full course CRUD (Admin)
- Course material upload/management (Lecturer)
- Student enrollment and withdrawal with duplicate prevention
- Course search and filtering by department and semester
- Course details and material viewer (Student)

### Member 03 — Marks & Grade Management (Anuda Pasandul, TG/2024/2101)

**Primary files:**
- `model/Evaluation.java`, `model/Mark.java`, `model/Grade.java`, `model/EligibilityRecord.java`
- `dao/EvaluationDAO.java`, `dao/MarkDAO.java`, `dao/GradeDAO.java`
- `service/MarkService.java`, `service/GradeService.java`, `service/GPAService.java`, `service/EligibilityService.java`
- `ui/lecturer/MarksPanel.java`, `ui/undergraduate/GradeViewPanel.java`

**Key features implemented:**
- Marks entry per evaluation (Quiz, Assignment, Mid-Exam, Final)
- Batch marks save with database transactions
- CA mark calculation using weighted evaluations
- Eligibility check: CA ≥ 40% AND Attendance ≥ 80%
- Grade calculation per UGC Circular No. 12-2024
- SGPA and CGPA calculation with credit weighting
- Degree class classification
- Repeat student grade cap at C/2.00

**Grading Scale (UGC Circular No. 12-2024):**

| Marks | Grade | Points |
|---|---|---|
| ≥ 85 | A+ | 4.00 |
| ≥ 80 | A | 4.00 |
| ≥ 75 | A- | 3.70 |
| ≥ 70 | B+ | 3.30 |
| ≥ 65 | B | 3.00 |
| ≥ 60 | B- | 2.70 |
| ≥ 55 | C+ | 2.30 |
| ≥ 50 | C | 2.00 |
| ≥ 45 | C- | 1.70 |
| ≥ 40 | D+ | 1.30 |
| ≥ 35 | D | 1.00 |
| < 35 | E | 0.00 |

> Repeat pass capped at **C (2.00)** regardless of actual marks.

### Member 04 — Attendance, Medical & Communication (M.D.R.L. Nonis, TG/2024/2064)

**Primary files:**
- `model/Attendance.java`, `model/AttendanceSummary.java`, `model/Medical.java`, `model/Notice.java`, `model/Timetable.java`
- `dao/AttendanceDAO.java`, `dao/MedicalDAO.java`, `dao/NoticeDAO.java`, `dao/TimetableDAO.java`
- `service/AttendanceService.java`, `service/MedicalService.java`, `service/NoticeService.java`, `service/TimetableService.java`
- `ui/technical/AttendancePanel.java`, `ui/technical/MedicalPanel.java`
- `ui/technical/TechnicalNoticePanel.java`, `ui/technical/TechnicalTimetablePanel.java`
- `ui/undergraduate/AttendanceViewPanel.java`, `ui/undergraduate/NoticeViewPanel.java`, `ui/undergraduate/TimetableViewPanel.java`

**Key features implemented:**
- Batch attendance recording for Theory and Practical sessions
- Individual student attendance view with session breakdown
- Attendance percentage calculation (including approved medical sessions)
- Five attendance scenarios pre-loaded (>80%, =80%, <80%, with/without medical)
- Medical certificate CRUD with Approve/Reject workflow
- Notice board with role-based audience targeting
- Timetable management with conflict detection

---

## Attendance Eligibility Rules

- Minimum **80% attendance** required for final exam eligibility.
- **Approved medical certificates** count as effective attendance.
- Effective attendance = (Present + Approved Medical) / Total Sessions × 100%

---

## Running Tests

The application connects to live MySQL. To verify all features:

1. **Start XAMPP MySQL** and ensure `faculty_academic_management` database is imported.
2. **Run the application** (Main.java).
3. **Login as admin** (`admin` / `admin123`) → verify Admin Dashboard loads.
4. **Login as lecturer** (`lec_kamal` / `lec123`) → verify Marks Panel.
5. **Login as technical officer** (`to_saman` / `to123`) → verify Attendance Panel.
6. **Login as student** (`tg20242001` / `student123`) → verify Grade and Attendance views.

---

## Known Limitations

1. The application targets a single local MySQL instance (XAMPP). Multi-user concurrent access is not tested.
2. Medical document file paths are stored as strings; actual file upload/storage is not implemented (path is recorded but file is not moved).
3. The application uses SHA-256 for password hashing. BCrypt would be preferred for production but requires an external library not included in the minimal Maven dependency list.
4. The SGPA calculator currently shows SGPA for all grades together — semester-specific filtering would require semester field on enrollments.

---

## Troubleshooting

| Issue | Fix |
|---|---|
| `Database Connection Notice` dialog on startup | Start XAMPP MySQL and ensure port 3306 is free. |
| `Unknown database 'faculty_academic_management'` | Import `database/group17_db.sql` via phpMyAdmin. |
| `Access denied for user 'root'@'localhost'` | Set `db.password` in `config.properties` to your MySQL root password. |
| Build fails — Maven not found | Run Maven from IntelliJ's Maven tool window, or install Maven and run `mvn clean compile`. |
| Application doesn't start — JDK version issue | Ensure JDK 17+ is selected in IntelliJ: File → Project Structure → SDK. |

---

## Git Workflow for Team Members

Each member should work in their own **feature branch**:

```bash
git checkout -b feature/member01-user-management   # Nethsara
git checkout -b feature/member02-course-management  # Sahan
git checkout -b feature/member03-marks-grades       # Anuda
git checkout -b feature/member04-attendance-comms   # Nonis
```

**Shared files** that require team coordination before modification:
- `config.properties` — DB settings
- `database/group17_db.sql` — Schema changes affect everyone
- `model/User.java` — Abstract base class
- `database/DatabaseConnection.java` — Connection manager
- `util/SessionManager.java` — Authentication state
- `ui/common/ModernTheme.java` — Shared styling constants

**Do not modify another member's primary files without discussion.**

---

## License

University of Ruhuna — Faculty of Technology  
ICT2132 OOP Practicum — Academic Year 2024/2025  
Group 17 — All rights reserved.
