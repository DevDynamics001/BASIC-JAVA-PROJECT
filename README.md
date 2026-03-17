# Bus Ticket Booking System

A robust, console-based Java application for bus ticket reservations, backed by an Oracle 10g Database using JDBC. This system is designed to provide seamless management of bus schedules, routes, seat assignments, and ticket bookings for both administrators and passengers.

## Features

### Admin Capabilities
- **Manage Routes**: Add, view, or delete travel routes (source, destination, distance, base fare).
- **Manage Buses**: Add, view, or remove buses, including type (e.g., AC Sleeper, Non-AC Seater) and seat capacity.
- **Manage Schedules**: Assign buses to routes with departure times, arrival times, and travel dates.
- **Manage Bookings**: View all user bookings or cancel specific bookings.
- **View Payments**: Monitor successful payment transactions.
- **View Seats**: Check seat availability and status for any specific bus.

### User Capabilities
- **Account Management**: Register as a new user or log in to an existing account.
- **Search Buses**: Browse available schedules based on travel dates and routes.
- **Book Tickets**: Select specific available seats, verify fare, and make reservations.
- **View Bookings**: Check the details of your past and upcoming ticket reservations.
- **Cancel Bookings**: Cancel your tickets if needed.

## Tech Stack
- **Language**: Java
- **Database**: Oracle Database 10g (Express Edition)
- **Database Connectivity**: JDBC (Java Database Connectivity)
- **Interface**: Command-Line Interface (CLI)
- **Architecture**: DAO (Data Access Object) Design Pattern

## System Architecture
The application follows a structured **Data Access Object (DAO)** architectural pattern to separate the core business logic and user interface from the database access mechanisms.

```text
+-------------------------------------------------------------------------+
|                        Presentation Layer (CLI)                         |
+------------------------------------+------------------------------------+
|             Admin Role             |             User Role              |
|------------------------------------|------------------------------------|
| - Manage Routes                    | - Register / Login                 |
| - Manage Buses                     | - Search Buses                     |
| - Manage Schedules                 | - Book Tickets                     |
| - View Bookings & Payments         | - View Past/Upcoming Bookings      |
| - Manage Seats & Cancellations     | - Cancel Bookings                  |
+------------------------------------+------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                       Data Access Layer (DAO)                           |
|-------------------------------------------------------------------------|
| AdminDAO, BusDAO, RouteDAO, BookingDAO, PaymentDAO, UserDAO, etc.       |
| (Handles SQL Queries, JDBC Connections, and Result Mapping)             |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                        Data Layer (Oracle DB)                           |
|-------------------------------------------------------------------------|
| Tables: users, admin, buses, routes, bookings, payments, seats, etc.    |
| (Relational Storage, Triggers, Sequences, Constraints)                  |
+-------------------------------------------------------------------------+
```

1. **Presentation Layer (CLI Interface)**
   - `MainLauncher`: The main entry point that verifies database connectivity and provides the main login/registration menu.
   - `MainAppAdmin` / `MainAppUser`: Handle the interactive command-line menus, capture user input, and display formatted output to the console.

2. **Data Access Layer (DAO)**
   - Located in the `dao` package, these classes (`AdminDAO`, `BookingDAO`, `BusDAO`, etc.) encapsulate all database interactions.
   - They execute SQL queries (CRUD operations) using JDBC and map the result sets to console output.
   - The connection logic is centralized in `DBconnection.java`.

3. **Data Layer (Oracle DB)**
   - An Oracle 10g database handles all persistent data storage, utilizing triggers and sequences for auto-generating primary keys, and cascading deletes for maintaining relational integrity.

## Database Schema Overview
The system utilizes a normalized relational database schema comprising several interconnected tables:
- `admin` & `users`: Authentication and user details.
- `buses`, `routes`, `route_stops`: Core inventory and travel path configuration.
- `bus_schedule`: Mapping buses to routes on specific dates/times.
- `seats`: Tracking availability of individual seats per bus.
- `bookings`, `ticket_passengers`, `payments`: Transactional data for user reservations, passenger details, and financial records.

## Setup Instructions

### Prerequisites
1. **Java Development Kit (JDK 8 or higher)** installed.
2. **Oracle Database 10g Express Edition** (or compatible Oracle DB) running on `localhost:1521`.
3. An IDE like Eclipse/IntelliJ or terminal capable of compiling Java files.
4. **Oracle JDBC Driver (`ojdbc14.jar` or similar)** added to your project's classpath.

### Database Setup
1. Connect to your Oracle database using SQL*Plus or any DB client as an authorized user.
2. Execute the `schema.sql` file provided in the repository to create the required tables, triggers, and sequences.
3. (Optional) Run `seed_data.sql` to populate the database with initial dummy data for testing.
4. Update `DBconnection.java` with your Oracle DB credentials (username/password) if they differ from the defaults.

### Running the Application
1. Compile the Java files in the `src` directory. You will need to include the Oracle JDBC driver in the classpath during compilation. From the project root (`d:\Bus_ticket`):
   ```bash
   javac -d bin -cp "C:/oraclexe/app/oracle/product/10.2.0/server/jdbc/lib/ojdbc14.jar;src" src/bus_ticket/*.java src/dao/*.java src/model/*.java
   ```

2. Run the `bus_ticket.MainLauncher` class. Make sure to include both the compiled output directory (`bin/`) and the JDBC driver in your classpath:
   ```bash
   java -cp "bin;C:/oraclexe/app/oracle/product/10.2.0/server/jdbc/lib/ojdbc14.jar" bus_ticket.MainLauncher
   ```

3. Upon startup, the application will verify the DB connection. If successful, you'll be greeted with the main menu.
4. **Default Admin Credentials**:
   - Username: `admin`
   - Password: `admin123`

## Project Structure
- `src/bus_ticket/` - Contains the main entry point (`MainLauncher`) and core UI loop classes (`MainAppAdmin`, `MainAppUser`).
- `src/dao/` - Contains all Database Access Object classes handling SQL queries (`AdminDAO`, `BookingDAO`, `BusDAO`, etc.).
- `src/model/` - Contains basic data model classes.
- `schema.sql` - Database schema definition.
- `seed_data.sql` - Sample data to get started quickly.

## License
This project is open-source. Feel free to use and modify the code for educational purposes.