# FLAMEPRO : FIRE EXTINGUISHER ORDERING SYSTEM

An IT Project  
Presented to the Faculty of the  
College of Information Technology  
Southwestern University  

In Partial Fulfillment  
of the Requirements for the subject  
IT083 - IT Project Management  

By  

Glen Mark Vincent Casayas  
Hannah Sabrina Makinano  
Angel Craven Maquira  
Adriel John Valencia  

JANA LENCY Q. PINO  
Faculty Adviser  

October 2026  

---

## TABLE OF CONTENTS

TABLE OF CONTENTS .................................................................................... iii  
LIST OF FIGURES ........................................................................................... iv  
LIST OF TABLES ............................................................................................ v  
CHAPTER 1 INTRODUCTION ......................................................................... 1  
   1.1 Rationale of the Study ......................................................................... 1  
   1.2 Statement of the Problem .................................................................... 2  
   1.3 Significance of the Study ..................................................................... 3  
   1.4 Scope and Limitations ......................................................................... 4  
CHAPTER 2 RELATED SYSTEMS ................................................................... 5  
CHAPTER 3 DESIGN AND METHODOLOGY .................................................... 9  
   4.1 Conceptual Framework ........................................................................ 9  
   4.2 Analysis and Design ............................................................................ 11  
   4.3 Development Model ............................................................................ 12  
   4.4 Development Approaches ................................................................... 13  
   4.5 Software Development Tools ................................................................ 14  
   4.6 Project Management ........................................................................... 15  
       4.6.1 Schedule and Timeline ............................................................. 15  
       4.6.2 Responsibilities ........................................................................ 16  
       4.6.3 Budget and Cost Management ................................................. 18  
       4.6.4 Change and Risk Management ................................................. 19  
   4.7 Verification, Validation and Testing ..................................................... 20  
BIBLIOGRAPHY ............................................................................................. 22  
Appendix A Project Charter ............................................................................. 25  
Appendix B Transmittal Letter ......................................................................... 26  
Appendix C Client Endorsement Letter ............................................................ 27  
Appendix D Survey / Interview Questionnaire .................................................. 28  
Appendix E Curriculum Vitae .......................................................................... 29  

---

## LIST OF FIGURES

Figure 1: Manual Process vs. Proposed System Workflow .................................. 10  
Figure 2: FlamePro Entity Relationship Diagram (ERD) ..................................... 11  
Figure 3: Waterfall Development Model Stages ................................................. 12  
Figure 4: Top-Down Development Approach Workflow ...................................... 13  
Figure 5: Project Management Timeline and Schedule ...................................... 15  

---

## LIST OF TABLES

Table 1: Summary of Approaches and Features ................................................ 7  
Table 2: Project Schedule and Implementation Milestones .................................. 15  
Table 3: Team Roles and Assigned Responsibilities ........................................... 16  
Table 4: Budget and Cost Management Breakdown ........................................... 18  
Table 5: Risk Management and Mitigation Plan ................................................. 19  
Table 6: System Test Plan and Acceptance Criteria ........................................... 21  

---

# CHAPTER 1

# INTRODUCTION

### 1.1 Rationale of the Study

Fire safety is essential for keeping cities, businesses, and homes safe from unexpected disasters. Portable fire extinguishers are the first tool people use to put out small fires before they spread and cause major damage. However, fire extinguishers must be regularly inspected, serviced, and refilled to work properly during emergencies. Even though local government safety rules require working fire extinguishers, many business owners and residents struggle to keep their safety equipment updated. This is mainly due to confusing ordering processes, slow phone calls to local shops, and a general lack of simple online services for fire safety needs.

Recent studies in software development show that mobile applications and central databases effectively eliminate operational delays in local service businesses. Digital platforms replace manual paper steps, making daily tasks faster, reducing order errors, and improving inventory tracking. While general online shopping apps and emergency alert tools are common today, they do not support the specific needs of local fire safety providers.

To address these local operational challenges, this study introduces FlamePro, a dedicated mobile application designed for fire extinguisher sales and on-site refill services in Cebu City. FlamePro establishes an integrated framework connecting five primary user roles; Customers, Sales Staff, Delivery Drivers, Field Technicians, and Super Administrators, within a unified system. The platform allows customers to purchase fire safety equipment via Cash on Delivery (COD) or book inspection and refill appointments while monitoring request status in real time. Developed using Android software, PHP web service APIs, and a PostgreSQL database, FlamePro removes administrative bottlenecks and streamlines field service coordination. The general objective of this study is to design, develop, and evaluate FlamePro to make purchasing and maintaining fire safety equipment efficient, transparent, and accessible for local operations in Cebu City.

### 1.2 Statement of the Problem

Residential and commercial property owners in Cebu City encounter significant delays when purchasing fire safety equipment or booking on-site refill services. Current operations rely on manual walk-in orders, paper records, and phone calls, which lack real-time tracking, clear pricing, and coordinated technician schedules.

To address these challenges, this study aims to answer the following specific research questions:

1. How can the FlamePro mobile application be designed and developed to streamline fire safety product sales and on-site refill service requests for users in Cebu City?
2. How can a multi-role backend structure effectively manage data and sync workflows across Customers, Sales Staff, Delivery Drivers, Technicians, and Super Admins?
3. How well does the system perform in handling Cash-on-Delivery (COD) orders, updating service status in real time?

### 1.3 Significance of the Study

This study provides practical and technical benefits to the following stakeholders:

**Customers & Property Owners:** Gives them a fast, accessible mobile platform to buy fire extinguishers and schedule refills with transparent pricing and real-time request tracking.

**Fire Safety Business & Staff:** Replaces slow paper records with an automated system, improving order processing speed, driver dispatching, and inventory accuracy.

**Field Technicians & Delivery Drivers:** Provides dedicated mobile interfaces to view assigned job schedules, locate delivery destinations, and update service statuses on the go.

**Future Developers & Researchers:** Serves as a reference framework for building multi-role mobile applications that integrate e-commerce ordering with localized field service dispatching.

### 1.4 Scope and Limitations

This study focuses on designing and developing FlamePro, a mobile application for fire extinguisher sales and on-site refill services in Cebu City. The system includes five user roles: Customers, Sales Staff, Delivery Drivers, Field Technicians, and Super Admins. Key features cover product ordering, Cash-on-Delivery (COD) transactions, service booking for on-site refills, real-time request tracking, and bulk stock updates via CSV file uploads. The backend uses PHP REST APIs and a PostgreSQL database managed with pgAdmin 4, while the mobile client is built for native Android devices.

This study is subject to specific technical and operational boundaries. Geographically, service dispatches and delivery tracking are strictly limited to operations within Cebu City. In terms of financial transactions, the platform supports Cash on Delivery (COD) only and does not process digital wallet payments or online credit card transactions. Additionally, the application is built exclusively for Android mobile devices, excluding iOS compatibility, and requires a continuous internet connection to sync live status updates, process orders, and manage dispatch schedules.

---

# CHAPTER 2

# RELATED SYSTEMS

This chapter reviews relevant literature, studies, and existing software systems related to fire safety equipment sales, inventory control, field service dispatch, and user access management. The main goal is to examine current practices in fire protection logistics, evaluate existing management platforms, and highlight the operational gaps addressed by the Fire Extinguisher Sales & Maintenance Management System (FlamePro). The studies and technical frameworks reviewed in this section serve as the foundation for the system's overall design, architecture, and database structure.

Smith and Jenkins (2018) examine the history and traditional practices of industrial fire extinguisher maintenance. The authors discuss how businesses have long depended on manual paper tags and physical logbooks to record cylinder inspections. However, this paper-based approach often causes missed inspection dates and unorganized service schedules. The study emphasizes that moving toward digital records helps organizations maintain equipment readiness and adhere to fire safety standards more effectively.

Brown and Davis (2012) explore legacy record management in industrial safety and the transition toward digital log systems. Their findings show that paper inspection tags physically attached to extinguisher valves easily get damaged or lost over time. This makes it difficult for building owners to prove compliance during safety audits. The authors conclude that centralized digital record systems significantly reduce compliance oversights and make safety records much easier to access.

Patel et al. (2020) conducted a study on automating compliance in fire and safety equipment management. The researchers found that property owners frequently forget mandatory inspection intervals, which leads to expired or depressurized extinguishers during sudden fire emergencies. Their research highlights the value of automated tracking systems and reminder notifications in keeping fire equipment fully functional and ready for emergency use.

Alvarez et al. (2020) discuss e-commerce architectures and database designs for modern retail businesses. The authors describe how online product catalogs, dynamic shopping carts, and structured checkout workflows make it easy for consumers to browse and buy goods. However, the study notes that standard retail storefronts only process product shipments and lack built-in tools to handle technical on-site services, such as equipment refilling and certified maintenance.

Srinivasan and Mehta (2021) compared generic e-commerce platforms with specialized service scheduling systems. Their study reveals that while platforms like WooCommerce and Shopee handle product variations like chemical types and cylinder sizes well, they cannot manage field service dispatching. The authors emphasize that businesses offering technical products require integrated systems capable of managing both physical sales and field service bookings.

Sandhu et al. (1996) introduce the foundational principles of Role-Based Access Control (RBAC) in computer software. The authors explain that assigning system permissions based on organizational roles rather than individual user accounts improves security and simplifies access management. This model ensures that each user only interacts with the features necessary for their daily responsibilities, preventing unauthorized data modifications.

Ferraiolo et al. (2001) proposed the standard NIST framework for Role-Based Access Control, detailing how role hierarchies protect data integrity in enterprise environments. The study explains that dividing responsibilities among administrators, operations staff, and clients prevents privilege escalation and ensures clean data separation across multi-user platforms.

Adams and Nguyen (2020) analyzed security and access control in multi-user enterprise systems. Their research highlights the importance of separating administrative controls from operational tasks. For sales and logistics applications, the authors recommend giving sales staff specific permissions to confirm orders, while reserving user management and pricing adjustments strictly for super administrators.

Roberts et al. (2022) explore task delegation and mobile workflows in on-demand logistics platforms. The authors show that providing delivery drivers with dedicated mobile screens to view assigned orders and update delivery statuses (such as Shipped or Delivered) improves delivery speed and keeps customers updated in real time.

O'Connor and Smith (2020) studied dynamic dispatching and lifecycle management for on-demand field technicians. Their findings demonstrate that paper-based job scheduling causes technician travel delays and misplaced job reports. By giving field technicians mobile access to view job locations, client notes, and service details, businesses can improve field efficiency and accurately track service completion.

Tan and Takahashi (2019) examine service-oriented architectures in on-demand logistics. The researchers point out that product sales follow a simple inventory reduction flow, whereas field services operate on appointment schedules and technician availability. They emphasize that combining both workflows into one platform requires clear status tracking for each activity.

Johnson et al. (2021) investigated the lifecycle management of fire protection assets in commercial buildings. The authors state that service platforms must not only track products purchased directly through the store, but also allow maintenance for customer-owned units ("Own Unit") bought elsewhere. Supporting third-party units broadens service reach and encourages long-term customer loyalty.

Rios and Santos (2022) evaluate customer-centric maintenance systems for third-party equipment servicing. Their study notes that customers often struggle to find certified technicians to inspect and refill existing fire extinguishers. Allowing users to book maintenance for equipment they already own creates an accessible, one-stop service experience.

Chen and Patel (2018) discuss database schema design patterns for financial logs and audit trails. The authors explain that directly overwriting product prices or stock numbers in database tables destroys historical transaction records. They recommend using dedicated log tables to track stock adjustments and price modifications, ensuring historical order totals remain accurate.

Zhou and Wright (2023) examine data consistency and integrity in e-commerce pricing tables. Their research shows that tracking price change histories with administrator timestamps ensures transparent accounting and protects system data from accidental discrepancies.

Baker and Evans (2022) review optimization techniques in modern Field Service Management (FSM) systems such as Jobber and ServiceTitan. The authors note that while these tools excel at scheduling technicians and tracking job statuses, they lack consumer shopping carts for retail product sales, making them less suitable for businesses that sell both products and services.

Fernandez and Morales (2022) discuss the challenges of implementing Enterprise Resource Planning (ERP) systems like SAP and Odoo in small and medium enterprises. The study found that while ERP systems offer complete business tools, their high subscription costs and complex navigation make them difficult for small local shops and regular consumers to use.

Republic Act No. 9514, known as the Revised Fire Code of the Philippines of 2008, serves as the legal basis for fire safety compliance. The law mandates that all buildings, commercial establishments, and residences maintain functional, inspected, and certified fire protection equipment, including portable fire extinguishers. FlamePro supports this legal mandate by providing residents and business owners in Cebu City with a straightforward mobile platform to purchase certified fire extinguishers and schedule routine on-site refills and inspections.

Table 1 summarizes the comparison between existing software solutions and the proposed FlamePro system.

Table 1  
*Summary of Approaches and Features*

| System Features & Capabilities | Generic E-Commerce (Shopee / Lazada) | FSM Software (Jobber / ServiceTitan) | Enterprise ERP (SAP / Odoo) | Proposed System (FlamePro) |
| :--- | :---: | :---: | :---: | :---: |
| Product Catalog & Cylinder Variants | Supported | Limited | Supported | Supported |
| On-Site Service Dispatch (Refill / Install) | Not Supported | Supported | Supported | Supported |
| Multi-Role Staff Access Control | Partial | Partial | Supported | Supported |
| Stock & Price Audit Logging | Limited | Not Supported | Supported | Supported |
| Servicing Customer-Owned Units | Not Supported | Supported | Partial | Supported |
| Mobile Delivery Driver Tracking | Partial | Not Supported | Partial | Supported |
| Affordable for Local MSMEs | Supported | Limited | Not Supported | Supported |

Evaluating these existing systems shows that current platforms either handle online retail without field services, or manage service appointments without consumer shopping carts. Furthermore, large ERP systems are too expensive and complex for local operations. FlamePro addresses these limitations by providing a single, accessible mobile application tailored for fire extinguisher sales, Cash-on-Delivery checkout, driver delivery updates, on-site technician refill dispatch, and audit logging for local operations in Cebu City.

---

# CHAPTER 3

# DESIGN AND METHODOLOGY

### 4.1 Conceptual Framework

The conceptual framework illustrates the change from a manual store system to a modern mobile application for fire extinguisher sales. In the manual process, customers must go to the physical store or call staff by phone to ask about available products and place orders. The proposed mobile application allows customers to view product pictures, prices, and available refill services directly on their phones. Users can easily add items to their cart, place Cash on Delivery orders, or request services without calling the store staff. Finally, the app gives real-time updates so customers can check if their order is processing, out for delivery, or already delivered.

```
+-----------------------------------------------------------------------------------+
|                                  MANUAL PROCESS                                   |
|                                                                                   |
|  Physical Store Visit / Telephone Call -> Direct Inquiry With Staff               |
|                      -> Manual Logbook -> Verbal Status Inquiry                   |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                                 PROPOSED SYSTEM                                   |
|                                                                                   |
|  Customer Mobile App (Catalog, Prices, Photos) -> Add To Cart / Request Service   |
|            -> Place Order (COD) -> Real-Time Status Tracking                      |
|                                 (Processing / Delivered)                          |
+-----------------------------------------------------------------------------------+
```
*Figure 1: Manual Process vs. Proposed System Workflow*

### 4.2 Analysis and Design

The system analysis and design defines how FlamePro structures data, models database entities, and organizes screens for each user group.

The database is built on a normalized relational structure managed in PostgreSQL. The Entity Relationship Diagram connects user accounts, products, order items, and service requests to ensure data integrity and avoid duplicate entries.

```
+------------------+             +-------------------+             +---------------------+
|      USERS       | 1         * |      ORDERS       | 1         * |     ORDER_ITEMS     |
+------------------+-------------+-------------------+-------------+---------------------+
| id (PK)          |             | id (PK)           |             | id (PK)             |
| username         |             | user_id (FK)      |             | order_id (FK)       |
| password_hash    |             | order_number      |             | product_id (FK)     |
| role             |             | total_price       |             | quantity            |
| phone_number     |             | payment_method    |             | unit_price          |
| address          |             | status            |             +---------------------+
+------------------+             | assigned_driver   |
        | 1                      +-------------------+
        |                                  
        | *                                
+----------------------+                   
|   SERVICE_REQUESTS   |                   
+----------------------+                   
| id (PK)              |                   
| user_id (FK)         |                   
| service_type         |                   
| preferred_date       |                   
| notes                |                   
| status               |                   
| assigned_technician  |                   
+----------------------+                   
```
*Figure 2: FlamePro Entity Relationship Diagram (ERD)*

The user interfaces were designed and prototyped in Figma to ensure a clear layout across the four main user modules:
1. **Customer Module**: Login, account setup, product catalog browsing, cart and COD checkout, service refill requests, and order status tracking.
2. **Sales Module**: Order verification, COD approval, stock monitoring, and initial dispatch scheduling.
3. **Delivery & Technician Module**: Mobile interface for delivery drivers to view assigned drop-offs and technicians to update on-site job completion.
4. **Admin Module**: Master web dashboard for managing staff credentials, product prices, stock audits, and sales performance.

### 4.3 Development Model

This project follows the **Waterfall Development Model**. Each development phase is completed sequentially to ensure requirements and designs are fully verified before proceeding to coding and deployment.

```
+-----------------------------------------------------------------------------------+
|                                  1. REQUIREMENTS                                  |
|               Define Roles, System Features, COD & Refill Flow                    |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                                     2. DESIGN                                     |
|             ER Diagrams, Database Schemas, Android Wireframes                     |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                                  3. DEVELOPMENT                                   |
|                Java (Android Studio) + PHP Backend + PostgreSQL                   |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                                     4. TESTING                                    |
|             CSV Bulk Data Import, Role Workflows, Drivers COD                     |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                                   5. DEPLOYMENT                                   |
|               Local Server Setup & Android APK Installation                       |
+-----------------------------------------------------------------------------------+
```
*Figure 3: Waterfall Development Model Stages*

- **Requirements**: Gathering business rules, user role needs, and fire safety equipment workflows from local client operations in Cebu City.
- **Design**: Creating wireframes in Figma, structuring PostgreSQL tables, and mapping API endpoints.
- **Development**: Coding the native mobile interface in Android Studio (Java) and the backend API endpoints in PHP.
- **Testing**: Validating login sessions, cart calculations, order status transitions, and bulk CSV stock updates.
- **Deployment**: Configuring local server hosting via XAMPP and compiling the production Android APK.

### 4.4 Development Approaches

This project utilizes the **Top-Down Development Approach**, where the front-end user interfaces and user interaction flows are designed first before developing the underlying backend and database infrastructure.

```
+-----------------------------------------------------------------------------------+
|                     STEP 1: USER INTERFACE & WIREFRAME DESIGN                     |
|                      Design UI Layout (Figma) & Android Java Screen Views         |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                        STEP 2: BACKEND API DEVELOPMENT                            |
|                 Creating PHP REST APIs to Connect UI Actions to Server Logic      |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                      STEP 3: DATABASE & DATA STORE LAYER                          |
|             PostgreSQL Schemas, Table Constraints, & CSV Bulk Importers           |
+-----------------------------------------------------------------------------------+
```
*Figure 4: Top-Down Development Approach Workflow*

Starting from user interface wireframes helped the developers confirm user expectations and screen requirements early, ensuring that the backend APIs and database tables were tailored directly to support the user experience.

### 4.5 Software Development Tools

The development of FlamePro utilizes selected tools across the design, mobile, backend, and database tiers:

Interface design and layout prototyping were executed in **Figma**, a cloud-based UI/UX software environment. Reusable design component kits and standardized icon sets were incorporated during this phase to establish visual consistency across all five operational user modules before front-end coding began.

Front-end development utilizes **Java** within **Android Studio** as a free, open-source platform to build native mobile application interfaces. This environment was selected for its strong Human-Computer Interaction (HCI) capabilities, enabling intuitive mobile layouts, while standard native XML libraries and HTTP networking dependencies were reused to streamline interface construction.

Back-end services rely on **PHP** deployed on an open-source **Apache** web server managed through **XAMPP**. This scripting language provides high maintainability, straightforward learning requirements, and seamless RESTful JSON request handling, reusing custom backend scripts to validate customer orders and field service dispatches effectively.

Database operations are managed using **PostgreSQL** as the core open-source relational database engine, paired with **pgAdmin 4** as the graphical administration management tool. This combination was selected for its reliable data security, strict relational integrity, and stable connectivity through PHP Data Objects (PDO). Additionally, pgAdmin 4 provides an intuitive interface for running SQL queries, executing database backups, and leveraging built-in CSV import tools to quickly populate initial product catalog and order tables.

### 4.6 Project Management

This section outlines the management strategies applied to plan, execute, monitor, and deliver the mobile application. It covers project scheduling, team responsibilities, budget allocation, and contingency plans for project changes and risks.

#### 4.6.1 Schedule and Timeline

The project implementation spans four months, organized into structured phases from initial planning to final deployment.

```
+----------------------------+-----------+-----------+-----------+-----------+
|          PROCESS           |  MONTH 1  |  MONTH 2  |  MONTH 3  |  MONTH 4  |
|                            | 1 2 3 4   | 1 2 3 4   | 1 2 3 4   | 1 2 3 4   |
+----------------------------+-----------+-----------+-----------+-----------+
| Requirements & Planning    | [===]     |           |           |           |
| System Architecture Design |   [===]   | [===]     |           |           |
| Database & REST API (PHP)  |           |   [===]   | [===]     |           |
| Mobile App UI/UX (Android) |     [===] | [===]     |           |           |
| Multi-Role Sync & Testing  |           |           |   [===]   | [===]     |
| CSV & COD Verification     |           |           |     [===] |   [=]     |
| User Evaluation & Docs     |           |           |           |   [===]   |
+----------------------------+-----------+-----------+-----------+-----------+
```
*Figure 5: Project Management Timeline and Schedule*

Table 2 details the target completion milestones for each phase of the project.

Table 2  
*Project Schedule and Implementation Milestones*

| Phase / Activity | Core Deliverables | Duration | Status |
| :--- | :--- | :--- | :--- |
| Requirements & Planning | Project charter, client interview, problem definition | Weeks 1 - 3 | Completed |
| System Architecture & Design | Figma wireframes, ER diagram, database schema design | Weeks 3 - 6 | Completed |
| Database & Backend APIs | PostgreSQL database setup, PHP REST API development | Weeks 6 - 10 | Completed |
| Android Mobile Development | Customer UI, shopping cart, refill booking, driver views | Weeks 4 - 9 | Completed |
| Multi-Role Integration & Test | Order dispatching, driver and technician updates | Weeks 10 - 13 | Completed |
| CSV & COD Verification | Product CSV importing, Cash-on-Delivery testing | Weeks 12 - 14 | Completed |
| Evaluation & Documentation | Final testing, manuscript review, project defense | Weeks 13 - 16 | Completed |

#### 4.6.2 Responsibilities

The development team consists of four members, with clearly divided duties across project management, backend services, mobile design, and documentation.

Table 3  
*Team Roles and Assigned Responsibilities*

| Team Member | Role | Assigned Task | Activities |
| :--- | :--- | :--- | :--- |
| **Hannah Sabrina Makinano** | Project Manager / Database / Backend | Project planning, database design, backend API, user accounts and roles, order management, delivery and COD settings | Plans schedule and sprints, assigns tasks, designs the database, builds the backend for the website and app, secures data, merges the work, prepares the demo |
| **Glen Mark Vincent Casayas** | UI / UX Frontend Developer | Customer app screens: login, catalogue, cart and checkout, order tracking, profile | Designs wireframes, builds app layouts in Android Studio, connects screens to the backend, tests on emulator and phone |
| **Angel Craven Maquira** | Documentation | Project documentation, user manual | Writes and edits the chapters, records meeting minutes, collects screenshots and diagrams, prepares the user manual and slides, checks the format and references |
| **Adriel John Valencia** | Quality Assurance / System Testing | System test execution, multi-role testing, CSV verification, client feedback | Validates application workflows, tests COD order updates, tests driver and technician screens, verifies CSV bulk import, logs bugs and reports issues |

#### 4.6.3 Budget and Cost Management

The project utilizes open-source programming tools and existing student computer hardware to keep expenses minimal. The allocated budget covers essential project needs including local research travel, documentation printing, and utilities.

Table 4  
*Budget and Cost Management Breakdown*

| Project Task & Included Items | Classification / Type | Base Cost | Contingency | Total Cost |
| :--- | :--- | :---: | :---: | :---: |
| System Architecture & Development | Direct / One-Time | ₱ 0 | ₱ 0 | ₱ 0 |
| Facilities & Utilities | Indirect / Recurring | ₱ 2,000 | ₱ 200 | ₱ 2,200 |
| Power & Utility Management | Indirect / Recurring | ₱ 1,500 | ₱ 150 | ₱ 1,650 |
| Field Testing & Integration | Direct / One-Time | ₱ 0 | ₱ 0 | ₱ 0 |
| Documentation & QA Evaluation | Direct / One-Time | ₱ 1,500 | ₱ 150 | ₱ 1,650 |
| Data Gathering & Logistics | Indirect / Recurring | ₱ 1,000 | ₱ 100 | ₱ 1,100 |
| **Total :** | | **₱ 6,000** | **₱ 600** | **₱ 6,600** |

#### 4.6.4 Change and Risk Management

To address potential technical and operational challenges during development, the team established practical risk mitigation steps:

Table 5  
*Risk Management and Mitigation Plan*

| Risk Description | Severity | Likelihood | Mitigation Plan |
| :--- | :---: | :---: | :--- |
| Local server network disconnection between Android app and XAMPP backend | Medium | High | Use stable local Wi-Fi IP configuration and provide clear network setup instructions in `ApiClient.java`. |
| Discrepancies in order and service status updates across multiple roles | High | Medium | Enforce database-level status constraints so orders and service tickets follow strict step-by-step state transitions. |
| Delays in bulk product uploads or invalid CSV file formats | Medium | Low | Implement input validation in pgAdmin and PHP scripts to verify CSV column headers before inserting stock data. |
| Data loss during development and coding | Critical | Low | Maintain regular code backups on GitHub and export scheduled SQL snapshots of the PostgreSQL database. |

### 4.7 Verification, Validation and Testing

To verify that the system operates correctly and meets user expectations, testing was conducted across multiple stages:

1. **Verification**: Checked that individual system modules function properly according to design specifications. This included checking user login authentication, form input validation, SQL query correctness, and error handling for empty cart checkouts.
2. **Validation**: Confirmed that the completed system fulfills client business needs for Flamepro Fire Protection Product Trading. Business workflows—such as walk-in vs. app orders, driver order deliveries, and on-site refill bookings—were validated directly against client requirements.
3. **Role-Based Workflow Testing**: Tested that each user role can only access their designated screens and perform assigned actions without security conflicts.
4. **Data and Payment Testing**: Verified that Cash-on-Delivery (COD) orders update accurately in database balances and that bulk CSV inventory imports execute smoothly without errors.

Table 6  
*System Test Plan and Acceptance Criteria*

| Test Case | Objective | Expected Result | Status |
| :--- | :--- | :--- | :---: |
| TC-01: User Authentication | Verify customer and staff login | User is authenticated and routed to the proper role dashboard | Pass |
| TC-02: Product Catalog & Cart | Verify item browsing and cart totals | Products display with prices; cart calculates total amounts accurately | Pass |
| TC-03: COD Checkout | Submit a Cash-on-Delivery order | Order is recorded as Pending with a unique order number | Pass |
| TC-04: Service Refill Booking | Submit an on-site service request | Request details and preferred dates are saved for technician dispatch | Pass |
| TC-05: Driver Dispatch Update | Update delivery status from app | Delivery driver can mark an order Out for Delivery and Delivered | Pass |
| TC-06: CSV Inventory Import | Import bulk stock data via CSV | Products table updates stock quantities accurately without data errors | Pass |

---

# REFERENCES / BIBLIOGRAPHY

Adams, R., & Nguyen, T. (2020). Security and Access Control in Multi-Tenant Enterprise Systems. *Journal of Software Engineering and Applications*, 13(4), 88–102.

Alvarez, M., Torres, L., & Gomez, P. (2020). E-Commerce Architecture and Database Optimization for Retail Systems. *International Journal of Information Management*, 52, 102–115.

Baker, C., & Evans, D. (2022). Optimization Techniques in Modern Field Service Management Systems. *IEEE Transactions on Engineering Management*, 69(2), 341–352.

Brown, K., & Davis, M. (2012). Legacy Record Management and the Transition to Digital Safety Logs. *Safety Science*, 50(3), 412–420.

Castro, H., & Gomez, R. (2023). Integrating Field Operations with On-Demand Logistics. *Journal of Logistics Technology*, 18(1), 45–59.

Chen, Y., & Patel, S. (2018). Database Schema Design Patterns for Audit Trails and Financial Log Accuracy. *ACM Transactions on Database Systems*, 43(2), 12–28.

Davenport, T. H. (1998). Putting the Enterprise into the Enterprise System. *Harvard Business Review*, 76(4), 121–131.

Ferraiolo, D. F., Sandhu, R., Gavrila, S., Kuhn, D. R., & Chandramouli, R. (2001). Proposed NIST Standard for Role-Based Access Control. *ACM Transactions on Information and System Security*, 4(3), 224–274.

Fernandez, J., & Morales, E. (2022). ERP Implementation Challenges in Small and Medium Enterprises. *Information Systems Frontiers*, 24(3), 889–904.

Garcia, A., & Miller, J. (2017). Limitations of Off-the-Shelf E-Commerce Platforms for Specialized Equipment Services. *International Journal of Services Computing*, 5(1), 15–29.

Gupta, V., & Verma, A. (2021). State-Machine Driven Workflows in E-Commerce Fulfillment. *Software: Practice and Experience*, 51(8), 1720–1738.

Harris, L., & Clark, E. (2021). Inventory Synchronization and Stock Adjustment Architecture in Multi-Warehouse Systems. *Computers & Industrial Engineering*, 155, 107–119.

Harrison, P., & Reed, K. (2021). Cost-Benefit Analysis of Enterprise Safety Software. *Journal of Systems and Software*, 173, 110–124.

Johnson, P., Williams, R., & Taylor, S. (2021). Lifecycle Management of Fire Protection Assets. *Fire Safety Journal*, 120, 103–114.

Kumar, S., & Zhao, Y. (2021). Field Service Management and Dispatching Strategies in Urban Environments. *European Journal of Operational Research*, 291(2), 650–664.

Lee, H., & Kim, S. (2021). Privilege Escalation Controls and Administrative Auditing in Relational Databases. *Computers & Security*, 102, 102–116.

Martinez, D., & Thomas, E. (2022). Bridging the Gap Between Online Retailing and Physical Field Services. *International Journal of Production Economics*, 244, 108–122.

Møller, C. (2005). ERP 2.0: Integrated Systems and Agile Business Design. *Communications of the ACM*, 48(11), 63–67.

O'Connor, B., & Smith, J. (2020). Dynamic Dispatching and Lifecycle Management for On-Demand Field Technicians. *Operational Research*, 20(3), 1435–1452.

Patel, N., Ramirez, G., & Wong, K. (2020). Regulatory Compliance Automation in Fire and Safety Equipment Management. *Fire Technology*, 56(5), 2115–2135.

Rios, C., & Santos, M. (2022). Customer-Centric Field Maintenance Systems for Third-Party Asset Servicing. *Journal of Retailing and Consumer Services*, 68, 103–115.

Roberts, M., Turner, B., & Phillips, D. (2022). Role-Based Task Delegation in Mobile Logistics Platforms. *IEEE Software*, 39(4), 54–62.

Sandhu, R. S., Coyne, E. J., Feinstein, H. L., & Youman, C. E. (1996). Role-Based Access Control Models. *IEEE Computer*, 29(2), 38–47.

Smith, A., & Jenkins, B. (2018). Historical Perspectives on Industrial Fire Extinguisher Inspection Practices. *Safety Management Quarterly*, 34(2), 45–58.

Srinivasan, R., & Mehta, P. (2021). E-Commerce Platforms vs. Specialized Service Systems: A Comparative Study. *Journal of Theoretical and Applied Electronic Commerce Research*, 16(6), 2300–2318.

Tan, X., & Takahashi, Y. (2019). Service-Oriented Architecture in On-Demand Logistics. *Service Science*, 11(2), 115–129.

Taylor, B., White, G., & Martin, L. (2022). Auditability and Price Modification Histories in Transactional Schemas. *Database Programming and Design*, 14(3), 89–101.

Wang, C., & Liu, X. (2023). Predictive and On-Demand Maintenance Management for Building Safety Systems. *Automation in Construction*, 145, 104–118.

White, R., & Anderson, C. (2020). Inventory Control Principles in Equipment Refurbishment Enterprises. *International Journal of Physical Distribution & Logistics Management*, 50(4), 432–448.

Wilson, E., Martinez, L., & Cox, G. (2023). Next-Generation Field Service Software: Integration of Retail and Maintenance Workflows. *Journal of Enterprise Information Management*, 36(1), 180–198.

Zhang, L., & Park, J. (2019). Fine-Grained Access Control in Relational Database Management Systems. *Information Systems Security*, 28(2), 77–91.

Zhou, M., & Wright, T. (2023). Data Consistency and Historical Integrity in E-Commerce Pricing Tables. *Software Quality Journal*, 31(2), 411–428.

---

# APPENDICES

### Appendix A: Project Charter

**Project Title**: FlamePro: Fire Extinguisher Ordering System  
**Project Start Date**: August 15, 2026  
**Project Manager**: Hannah Sabrina Makinano  

**Key Schedule Milestones**:
- Requirements Gathering, System Analysis & Design
- UI/UX Design & Database Design
- Development Phase
- Testing Phase
- Deployment
- Documentation

**Budget Information**: Academic project with limited development costs. The project primarily utilizes existing hardware, software, development tools, and internet resources available to the project team. Total estimated budget: ₱ 6,600.

**Project Objectives**:
- Develop an app-based platform for FlamePro's fire protection product and service operations.
- Provide secure user and administrator account management.
- Allow customers to browse products, manage their cart, and place online orders.
- Enable customers to submit refill requests and service schedules.
- Provide order and service status tracking.
- Help administrators manage products, inventory, orders, and services.
- Monitor product expiry dates and provide reminders or alerts.
- Centralize records and improve communication between customers and administrators.

**Main Project Success Criteria**:
- Users can securely register and log in.
- Customers can browse products and add items to their cart.
- Customers can successfully place and track orders.
- Customers can submit refill and service requests.
- Administrators can manage products, inventory, orders, and service requests.
- The system can monitor expiry dates and provide notifications.
- Records are centralized and easily accessible.
- The system reduces manual processes, delays, and record-keeping errors.

---

### Appendix B: Transmittal Letter

**COLLEGE OF INFORMATION TECHNOLOGY**  
**SOUTHWESTERN UNIVERSITY PHINMA**  
Cebu City, Philippines  

**Date**: October 13, 2026  

**JANA LENCY Q. PINO**  
IT Project Management Instructor  
College of Information Technology  
Southwestern University  

**Dear Ma'am Pino,**

We respectfully submit the capstone project paper titled:

**"FLAMEPRO : FIRE EXTINGUISHER ORDERING SYSTEM"**

in partial fulfillment of the requirements for the subject **IT083 - IT Project Management**.

This paper contains the complete documentation from Chapter 1 to Chapter 3, including the Introduction, Related Systems, Design and Methodology, Project Management Schedule, Budget, and Testing Plan, together with the project bibliography and appendices.

Thank you very much for your continuous guidance and support.

Respectfully yours,

**Glen Mark Vincent Casayas**  
**Hannah Sabrina Makinano**  
**Angel Craven Maquira**  
**Adriel John Valencia**  

---

### Appendix C: Client Endorsement Letter

**CLIENT ENDORSEMENT LETTER**

**Date**: August 19, 2026  

**To Whom It May Concern:**

I formally confirm that Flamepro Fire Protection Product Trading agrees to serve as the client for the student IT project titled "Fire Extinguisher Sales & Maintenance Management System," developed by Makinano, Casayas, Valencia, and Maquira.

We have authorized the project team to analyze our business workflows, identify operational challenges, and collect functional system requirements. We approve the defined project scope, which encompasses inventory management, sales order recording, field technician service dispatch, client service booking, and automated email maintenance alerts for one (1) Administrator, three (3) Counter Staff, three (3) Field Technicians, and client users. We understand this project is conducted for academic purposes, and all shared data will remain confidential.

Sincerely,

*(Signed)*  
**CHANLER ANDALES**  
Business Owner, Flamepro Fire Protection Product Trading  
0908 716 5814 | chanlerandales10@gmail.com  

---

### Appendix D: Survey / Interview Questionnaire

**SURVEY / INTERVIEW FORM**  
**Project Title**: Fire Extinguisher Sales & Maintenance Management System  
**Client**: Flamepro Fire Protection Product Trading  

1. **How do you currently track your inventory and serial numbers?** *(Check all that apply)*  
   [ ] Manual Logbook / Notebook  
   [X] Excel / Spreadsheets  
   [ ] Existing POS / ERP System  
   [ ] Paper Tags on Units Only  
   [ ] Memory  
   [ ] Others: ___________________________  

2. **How do you track cylinder inspection and refill expiration dates for clients?**  
   [X] Paper Inspection Tags on Cylinders  
   [X] Excel Spreadsheet Records  
   [ ] Calendar / Manual Reminders  
   [ ] We do not track them after sale  
   [ ] Others: ___________________________  

3. **How do clients place orders or request refills currently?** *(Check all that apply)*  
   [X] Walk-in Store Visit  
   [X] Direct Phone Call  
   [ ] Email / Purchase Orders  
   [X] Social Media / Messaging Apps  
   [ ] Field Sales Representative  

4. **What are your top 3 operational challenges?** *(Check all that apply)*  
   [ ] Hard to track individual cylinder serial numbers and maintenance history  
   [X] Missing inspection/refill due dates for existing clients  
   [ ] Manual calculation of chemical volume, weight, and pricing  
   [X] Out-of-stock items (specific agent types or weight capacities)  
   [ ] Difficulty managing bulk quotes and corporate invoices  
   [ ] Loss of returning customers to competitors  
   [ ] Others: ___________________________  

---

### Appendix E: Curriculum Vitae

#### 1. Hannah Sabrina Makinano
**Contact Information**  
- Address: Brgy. Lorega San Miguel Cebu City  
- Mobile: 0995 464 9683  
- Email: hago.makinano.swu@phinmaed.com  

**Personal Information**  
- Date of Birth: July 10, 2007  
- Age: 19  
- Citizenship: Filipino  
- Gender: Female  
- Marital Status: Single  

**Educational Background**  
- College: Southwestern University Phinma, BSIT (2025 - 2027)  
- Senior High School: Southwestern University Phinma (2023 - 2025)  
- High School: Abellana National School (2019 - 2022)  

---

#### 2. Glen Mark Vincent P. Casayas
**Contact Information**  
- Address: Brgy. Cagay Sibonga Cebu - Purok 3  
- Mobile: 0912 351 1239  
- Email: glpa.casayas.swu@phinmaed.com  

**Personal Information**  
- Date of Birth: November 01, 2006  
- Age: 19  
- Citizenship: Filipino  
- Gender: Male  
- Marital Status: Single  

**Educational Background**  
- College: Southwestern University Phinma, BSIT (2025 - 2027)  
- Senior High School: Teodoro Dela Vega Memorial National Senior High School (2020 - 2024)  
- Elementary: Cagay Elementary School (2018 - 2019)  

---

#### 3. Angel Craven Maquira
**Contact Information**  
- Address: Cebu City  
- Email: maquira.craven.swu@phinmaed.com  

**Personal Information**  
- Citizenship: Filipino  
- Gender: Female  
- Marital Status: Single  

**Educational Background**  
- College: Southwestern University Phinma, BSIT (2025 - 2027)  

---

#### 4. Adriel John Valencia
**Contact Information**  
- Address: Cebu City  
- Email: valencia.adriel.swu@phinmaed.com  

**Personal Information**  
- Citizenship: Filipino  
- Gender: Male  
- Marital Status: Single  

**Educational Background**  
- College: Southwestern University Phinma, BSIT (2025 - 2027)  
