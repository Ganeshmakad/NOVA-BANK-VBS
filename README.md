# Nova Bank – Virtual Banking System

Nova Bank is a Virtual Banking System developed as a mini project using Spring Boot.  
The project demonstrates basic banking operations with separate roles for Admin and User.

---

## Project Objective
To build a simple and centralized system that simulates core banking activities such as user management, transactions, notifications, and support handling.

---

## Features

### User
- User registration and login  
- View account dashboard and passbook  
- Transfer amount (simulation)  
- View notifications  
- Raise support queries  

### Admin
- Admin login  
- Create and manage users  
- View transaction and system history  
- Send notifications to users  
- Respond to and close user support queries  

---

## Technology Used
- Java  
- Spring Boot  
- HTML, CSS, JavaScript  
- MySQL  
- Maven  

---

## Project Structure
```
src/main/java/com/vbs/demo
├── controller
├── dto
├── models
├── repositories
└── VbsApplication.java
```

---

## How to Run
1. Open the project in IntelliJ or Eclipse  
2. Configure MySQL database in `application.properties`  
3. Run the application using:
```
mvn spring-boot:run
```
4. Open browser and go to:
```
http://localhost:8080/home.html
```

---

## Note
This project is developed for academic purposes to understand Spring Boot, MVC architecture, and database connectivity.

---

## Author
Ganesh Makad  
Computer Engineering Student

