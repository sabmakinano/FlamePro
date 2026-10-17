# FlamePro: Fire Extinguisher Ordering & Maintenance System

FlamePro is a native Android mobile and web-based application designed for fire safety equipment sales, on-site refilling services, and maintenance scheduling in Cebu City.

## Group Members (SWU-003)
* **Hannah Sabrina Makinano** - Project Manager / Backend & Database
* **Glen Mark Vincent Casayas** - UI/UX / Android Frontend Developer
* **Angel Craven Maquira** - Technical Documentation / QA - but not doing his part, sumbits different file not connected to the project
* **Adriel John Valencia** - System Testing & Quality Assurance - also not doing his part, always absent and not replying

## Technology Stack
* **Mobile Client:** Android Studio (Java 17, Android SDK 34)
* **Web Portal:** HTML5, CSS3, JavaScript (Hosted on Apache)
* **Backend:** PHP 8.2 (RESTful APIs)
* **Database:** PostgreSQL 16 (pgAdmin 4, PDO)

## Setup and Installation

### 1. Database Setup
1. Open **pgAdmin 4** and create a database named `flamepro_db`.
2. Open the Query Tool and execute the script located at `/backend/database/flamepro_db.sql`.

### 2. Backend API Setup
1. Copy the `/backend/flamepro_api/` directory into your Apache server directory (e.g., `xampp/htdocs/`).
2. Copy `config/db.example.php` to `config/db.php` and update it with your local PostgreSQL credentials.
3. Start Apache in the XAMPP Control Panel.

### 3. Android Mobile App Setup
1. Open the project in **Android Studio**.
2. Sync Gradle files.
3. Run the application on an Android Emulator or physical device (Min SDK: 26).