# Course Review CRUD Application

A full-stack desktop application built with **Java**, **JavaFX**, and **SQLite** that allows authenticated users to create, read, update, and delete course reviews.

The system implements persistent storage, relational database design, and multi-scene GUI navigation to provide a complete end-to-end CRUD workflow.

---

## 🚀 Tech Stack

- **Language:** Java (JDK 21)
- **Frontend:** JavaFX
- **Database:** SQLite (file-based)
- **Database Access:** JDBC
- **Build Tool:** Gradle

---

## 📌 Features

### 🔐 Authentication
- User account creation with unique username constraint
- Password validation (minimum length enforcement)
- Secure login workflow
- Session-based user state management

### 📚 Course Management
- Add new courses with input validation
- Search courses by subject, number, or title (case-insensitive)
- Display dynamic average ratings (formatted to two decimal places)

### ⭐ Review System
- Create, edit, and delete course reviews
- Enforced one-review-per-user-per-course constraint
- Automatic timestamp generation on submission
- Persistent storage across application restarts

---

## 🗄️ Database Design

- File-based SQLite database
- Relational schema structured in **Second Normal Form (2NF)**
- Foreign key relationships linking:
  - Users → Reviews
  - Courses → Reviews
- Application-level and database-level integrity constraints

---

## 🖥️ Application Architecture

- Multi-scene JavaFX GUI (Login, Course Search, Reviews)
- Separation of concerns between:
  - UI layer
  - Business logic
  - Data access layer
- Dynamic query filtering for search functionality
- Robust in-application error handling (no console dependency)

---

## 🛠️ Running the Application

```bash
./gradlew run
