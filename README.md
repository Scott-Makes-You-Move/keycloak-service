# Keycloak Service

This project extends Keycloak by implementing a **custom event listener** that listens for **user creation and deletion** events and triggers HTTP requests to an external service.

## 🚀 Features
- **Custom Keycloak Event Listener**: Handles user-related admin events.
- **Profile-based Configuration**: Supports `local` and `cloud` profiles.
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

## 🏗️ Building the Project
To build the project, use **Maven profiles**:

```sh
# Build with the local profile
mvn -Plocal clean package

# Build with the cloud profile
mvn -Pcloud clean package
```
This will package the correct application-<profile>.properties file.

### 🐳 Running in Docker

Build and run the Keycloak container:
```
docker build -t smym-keycloak .
docker run --name smym-keycloak -p 8080:8080 -e ACTIVE_PROFILE=cloud smym-keycloak
```

### 🔧 Configuration

The project supports profile-based configuration with property files:
•	application-local.properties
•	application-cloud.properties

To set the active profile when running the container:
```
docker run --name smym-keycloak -p 8080:8080 smym-keycloak -e ACTIVE_PROFILE=cloud 
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

### 🏗️ Building with a Profile
```
mvn -Pcloud clean package
```

### 🔧 Configuration

Properties are loaded based on the active profile. Example property files:
•	src/main/resources/application-local.properties
•	src/main/resources/application-cloud.properties

### 📦 Deployment

The built JAR is located in:
```
target/keycloak-extensions-1.0-SNAPSHOT.jar
```
This is copied into the Keycloak service during the Docker build process.