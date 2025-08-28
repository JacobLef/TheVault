# TheVault

TheVault is a custom relational database engine built from scratch in Java, designed to power a multi-bank
banking application. This project implements core database concepts including custom B+ Tree data 
structures for efficient storage and retrieval, a complete relational schema with tables and indexes, 
password salting and hashing, and a clean MVC architecture supporting multiple concurrent users.

The system features a banking domain with full CRUD operations for users and accounts, secure 
authentication, transaction logging, and multi-bank management capabilities. Built with Docker 
containerization and comprehensive test coverage, TheVault demonstrates practical application of 
computer science fundamentals including data structures, algorithms, system design, and 
software engineering principles.

Key Technologies: Java, Docker, JUnit 5, Custom B+ Tree Implementation, Relational Database Design

## Clone the Repository
```bash
    git clone https://github.com/JacobLef/TheVault
```

## Docker Configuration: Build and Run
````bash
    docker build -t banking-database .    
````
```bash
    docker run -p 8080:8080 banking-database
```

#### To use docker compose instead:
```bash
    docker compose up
```
#### To use docker compose started in the background:
````bash
    docker compose -d up
````