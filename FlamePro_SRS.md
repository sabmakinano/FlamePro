
# SOFTWARE REQUIREMENTS SPECIFICATION (SRS)

**Project Title:** FlamePro — Fire Extinguisher Ordering System  
**Prepared by:** Glen Mark Vincent Casayas, Hannah Sabrina Makinano, Angel Craven Maquira, Adriel John Valencia  
**Subject:** IT083 – IT Project Management  
**Institution:** Southwestern University PHINMA  
**Date:** October 2026  

---

## 1. System Overview

FlamePro is a mobile application for Android that allows customers to purchase fire extinguishers and book on-site refill services using Cash on Delivery (COD). The backend runs on a PHP REST API connected to a PostgreSQL database. A separate web-based admin portal handles staff management, order dispatching, and sales monitoring.

---

## 2. User Roles

| Role | Description |
| :--- | :--- |
| Customer | Browses products, places COD orders, books refill services, and tracks order status |
| Sales Staff | Approves orders, monitors stock, and coordinates delivery assignments |
| Delivery Driver | Views assigned deliveries and updates order status |
| Field Technician | Views assigned service tickets and updates job completion status |
| Super Admin | Full access — manages staff accounts, products, pricing, and all system records |

---

## 3. Functional Requirements

**FR-01 — User Registration**
- Customers register using a unique username, email, and password
- Password must be at least 6 characters
- Duplicate usernames or emails are rejected
- Passwords are stored as bcrypt hashes in the database

**FR-02 — User Login and Role Routing**
- Users log in using their username or email and password
- The system checks credentials against the database
- After login, the user is routed to their role-specific screen
- Deactivated accounts cannot log in

**FR-03 — Product Catalog**
- Products are grouped by category (Fire Extinguishers, Sprinklers, Fire Hose, Fireman Equipment, Others)
- Each product shows name, price, discounted price, rating, and stock status
- Out-of-stock products cannot be added to the cart

**FR-04 — Shopping Cart and COD Checkout**
- Customers can add products, adjust quantities, and remove items from the cart
- The cart shows a live total price calculation
- At checkout, customers enter their shipping address and confirm Cash on Delivery payment
- The system generates a unique order number (FP-YYYYMMDD-XXXX) and saves the order as Pending

**FR-05 — Service Booking**
- Customers can request on-site services: Refilling, Hydrostatic Testing, Installation, or Inspection
- Customers select a preferred date and time and add equipment notes
- Service requests are saved with status Pending and queued for technician assignment

**FR-06 — Order Tracking**
- Customers can view their order history and live order status
- Order status flow: Pending → Out for Delivery → Delivered (or Cancelled)

**FR-07 — Delivery Driver Workflow**
- Drivers see only the orders assigned to them
- Drivers update delivery status: Assigned → Out for Delivery → Delivered

**FR-08 — Technician Workflow**
- Technicians see only the service tickets assigned to them
- Technicians update job status: Assigned → In Progress → Completed

**FR-09 — Admin and Sales Web Portal**
- Admins and Sales staff log in through a separate web portal
- Admins assign delivery riders to pending orders
- Admins assign technicians to pending service requests
- Admins add, edit, and deactivate staff accounts
- The portal displays KPI analytics: total orders, revenue, and active staff

**FR-10 — Profile Management**
- Customers can update their name, phone number, and delivery address
- Changes are saved to the database and reflected immediately in the app

---

## 4. Non-Functional Requirements

| ID | Requirement | Description |
| :--- | :--- | :--- |
| NFR-01 | Security | All passwords use bcrypt hashing. All SQL queries use prepared statements to prevent SQL injection. |
| NFR-02 | Performance | API responses must load within 3 seconds on a stable Wi-Fi or mobile data connection. |
| NFR-03 | Compatibility | Android app runs on Android 8.0 (API Level 26) and above. Web portal works on Chrome, Edge, and Firefox. |
| NFR-04 | Usability | The mobile interface follows Google Material Design guidelines with clear buttons and readable text. |
| NFR-05 | Reliability | The system handles empty carts, duplicate orders, and network failures with clear error messages. |
| NFR-06 | Maintainability | Backend API endpoints are organized into separate PHP files by function (auth, orders, products, delivery, technician). |
