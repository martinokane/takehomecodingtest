# Barclays Take Home Coding Test - Martin O'Kane

This repository contains Martin O'Kane's submission for the Take Home Coding Test for Barclays.

<b>Thank you for taking the time to review this!</b>

This submission contains a Spring Boot application, which will run an API containing a number of endpoints relating to banking transactions.

## Running the Application

### Requirements

- JDK 17 or later
- Docker

#### Docker setup

This project uses a database to manage state. It was developed specifically with a postgres 17 container. The docker-compose file to build this has been included, and a running database is required for the application to start.

To create a blank database, in the project root directory use the following command: `docker-compose up`.

The application will populate the database with the required tables on first startup.

To delete the database and associated volumes, use the following command: `docker-compose down -v`.

#### Gradle

This project was built using Gradle and can be interacted with using the included Gradle wrapper `gradlew` / `gradlew.bat`.

To start the application, run the following command in the root directory of the repo location on your machine:

    ./gradlew bootRun

Once started, this will begin hosting at http://localhost:8080

The server will also host a swagger-ui page at http://localhost:8080/swagger-ui/index.html from which you can get an auto-generated list API specifications and test the implemented endpoints in your browser. Please note that this is not comprehensive or particularly detailed, and is just intended to give a quick snapshot of available endpoints and their expected payloads / success responses.

Alternatively please see the included [OpenAPI spec file](/openapi.yaml) - which is unchanged from the original excepting the addition of a login endpoint.

## Testing

The application features a full suite of JUnit unit tests that can be run using the following command from the root directory of the repo:

    ./gradlew clean test

## API Usage

<i>Please see the [OpenAPI spec file](/openapi.yaml) or use the [embedded swagger-ui](http://localhost:8080/swagger-ui/index.html) (while the application is running) for additional documentation.</i>

With the exception of the 'Create User' and 'Login' endpoints, requests must have an `Authorization` header with a valid JWT obtained from this application's 'Login' endpoint:

    {"Authorization": "Bearer <token>"}
