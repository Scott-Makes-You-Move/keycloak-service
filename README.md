# Keycloak Service

This project extends Keycloak by implementing a **custom event listener** that listens for **user creation and deletion** events and triggers HTTP requests to an external service.

## 🚀 Features
- **Custom Keycloak Event Listener**: Handles user-related admin events.
- **Dockerized Deployment**: Runs as a containerized service with Keycloak.
- **PostgreSQL Database Integration**: Uses PostgreSQL as the Keycloak database.

## 🛠️ Project Structure
```
keycloak-service/
│── keycloak-extensions/   # Contains custom Keycloak extensions
│   ├── src/
│   ├── target/
│   ├── pom.xml
│── Dockerfile             # Builds and runs the Keycloak service
│── pom.xml                # Main project configuration
```

## ⚙️ Requirements
- **Java 17+**
- **Maven**
- **Docker**
- **Keycloak 23.0.7**
- **PostgreSQL (Optional, if using a database)**

### 🐳 Running in Docker

Build and run the Keycloak container:
```
docker build -t smym-keycloak .
docker run --name smym-keycloak -p 8080:8080 smym-keycloak
```

## Keycloak Extensions

This module contains custom Keycloak extensions, specifically an event listener that reacts to user creation and deletion.

### 🚀 Features
•	Event Listener for Admin Events

•	Triggers HTTP Requests when users are created or deleted

•	Profile-based Configuration for environment-specific settings

### 🏗️ Building the Extensions Module
```
mvn clean package
```

### 📦 Deployment

The built JAR is located in:
```
target/keycloak-extensions-1.0-SNAPSHOT.jar
```
This is copied into the Keycloak service during the Docker build process.
