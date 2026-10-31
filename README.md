# 🚗 ParkSmart - Parking Management System

A robust, secure desktop Parking Management System built with **Java Swing**, **Object-Oriented Programming (OOP)** principles, and a **MySQL / CSV persistence layer** using the **Data Access Object (DAO)** pattern.

---

## 📌 Project Overview
ParkSmart manages vehicle entry, slot allocations, exit billing, live slot grid tracking, audit records, and analytics. It strictly follows OOP principles (Abstraction, Encapsulation, Inheritance, Polymorphism) and cleanly separates GUI presentation from business services and data access.

### 🌟 Key Features
- **Intuitive Dashboard**: Overview cards with live capacity, active parked vehicles, and total collected revenue.
- **Vehicle Entry & Ticket Generation**: Supports **Bike**, **Car**, and **Bus** with automated slot assignment and itemized ticket receipts.
- **Interactive Visual Slot Grid**: Color-coded bay layout (Vacant / Occupied) with live filtering by vehicle type.
- **Vehicle Exit & Fee Settlement**: Automated hourly tariff calculation (with minimum 1-hour charging and partial-hour rounding), itemized departure billing, and slot release.
- **Comprehensive Audit Records**: Searchable and filterable table logs of all current and historical parking sessions.
- **Revenue Analytics & Daily Reports**: Breakdown by vehicle category and daily collections.
- **Enterprise-Grade Architecture**: Zero inline SQL in the UI controllers; abstracted database operations via `ParkingSlotDao` and `ParkingTicketDao` with automatic failover to CSV storage.

---

## 🏗️ Architecture & OOP Design

- **Polymorphism & Inheritance**: Abstract base class `Vehicle` extended by `Bike`, `Car`, and `Bus` with polymorphic hourly rate calculations.
- **Encapsulation**: Private fields, protected invariants, and rigorous validation for parking tickets and slots.
- **DAO Pattern**: Complete separation of persistence logic (`com.parking.dao`) from business workflows (`com.parking.service`) and graphical views (`com.parking.ui`).
- **Resilient Persistence**: Seamless live operation with **MySQL 8.0+** using JDBC with automatic fallback to local CSV file backup.

---

## 📂 Project Structure
```text
mini pro/
├── src/
│   └── com/parking/
│       ├── Main.java                    # Application entry point
│       ├── dao/                         # Data Access Objects (DAO pattern)
│       │   ├── ParkingSlotDao.java
│       │   ├── ParkingSlotDaoImpl.java
│       │   ├── ParkingTicketDao.java
│       │   └── ParkingTicketDaoImpl.java
│       ├── data/                        # Database connectivity & schema initializer
│       │   ├── DatabaseConnection.java
│       │   └── DatabaseInitializer.java
│       ├── model/                       # OOP Domain Models
│       │   ├── Vehicle.java             # Abstract base class
│       │   ├── Bike.java
│       │   ├── Car.java
│       │   ├── Bus.java
│       │   ├── VehicleType.java
│       │   ├── ParkingSlot.java
│       │   └── ParkingTicket.java
│       ├── service/                     # Business Logic Services & Facade
│       │   ├── ParkingManager.java      # Central facade
│       │   ├── ParkingSlotService.java
│       │   ├── ParkingTicketService.java
│       │   └── ParkingFeeCalculator.java
│       └── ui/                          # Java Swing User Interface
│           ├── MainFrame.java           # Window container & sidebar
│           ├── UITheme.java             # Palette & styling utilities
│           ├── DashboardPanel.java
│           ├── VehicleEntryPanel.java
│           ├── VehicleExitPanel.java
│           ├── ParkingSlotsPanel.java
│           ├── RecordsPanel.java
│           └── ReportsPanel.java
├── libs/                                # MySQL JDBC Connector JAR
├── data/                                # Local backup records (CSV)
├── db.properties                        # Database connection configuration
├── parking_management.sql               # MySQL Schema & seed script
└── run.bat                              # One-click execution script
```

---

## 🚀 How to Run the Project

### Option 1: IntelliJ IDEA (Recommended)
1. Open **IntelliJ IDEA**.
2. Select **File > Open...** and navigate to this folder.
3. Mark `src` as the **Sources Root** if prompted.
4. Ensure the MySQL connector in `libs/mysql-connector-j.jar` is added under **File > Project Structure > Libraries**.
5. Locate `src/com/parking/Main.java`, right-click, and select **Run 'Main.main()'**.

### Option 2: Command Line / Batch Script (Windows)
Double-click `run.bat` in the root directory, or execute:
```cmd
javac -cp "libs/*;." -d out src/com/parking/**/*.java
java -cp "out;libs/*" com.parking.Main
```

---

## 🗄️ Database Setup (MySQL)
1. Open MySQL Workbench or MySQL Command Line Client.
2. Execute the included `parking_management.sql` script:
   ```sql
   SOURCE parking_management.sql;
   ```
3. Update `db.properties` with your database credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/parking_management_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
   db.user=root
   db.password=YOUR_PASSWORD
   ```
*(Note: If MySQL is not running, the application will automatically run in local resilient mode using CSV storage.)*
