# Udemy Spring Microservices

My solutions for the Udemy course "Master Microservices with Spring Boot and Spring Cloud": a REST API first, then a set
of Spring Cloud microservices that find each other through Eureka and sit behind an API gateway. Everything runs on
Java 25; the databases are in-memory H2, so nothing has to be installed.

| #   | Section                 | Project                                          | Covers                                                                                                   |
| --- | ----------------------- | ------------------------------------------------ | -------------------------------------------------------------------------------------------------------- |
| 001 | RESTful Web Services    | [Social media API](001-Restful-Web-Services)     | users and posts with JPA + H2, validation, problem-detail errors, HATEOAS links, JSON / XML, API versioning by header, query parameter and media type, basic auth |
| 002 | Microservices           | [Currency services](002-Microservices)           | config server, limits service, currency exchange + conversion, Eureka naming server, Spring Cloud Gateway, OpenFeign, Resilience4j, Docker Compose |

### 002 – Services

| Service                                                         | Port | What it does                                                                     |
| --------------------------------------------------------------- | ---- | -------------------------------------------------------------------------------- |
| [naming-server](002-Microservices/naming-server)                | 8761 | Eureka registry that every other service registers with                          |
| [config-server](002-Microservices/config-server)                | 8888 | serves the files in [config-repo](002-Microservices/config-repo), per profile    |
| [limits-service](002-Microservices/limits-service)              | 8080 | `GET /limits`, read from the config server                                       |
| [currency-exchange-service](002-Microservices/currency-exchange-service) | 8000 | `GET /currency-exchange/from/USD/to/EUR`; `/sample-api` shows retry, circuit breaker and rate limiter |
| [currency-conversion-service](002-Microservices/currency-conversion-service) | 8100 | `GET /currency-conversion/from/USD/to/EUR/quantity/10`, calls the exchange service through Feign |
| [api-gateway](002-Microservices/api-gateway)                    | 8765 | routes `/currency-exchange/**`, `/currency-conversion/**` and the short `/convert/**` |

## Run

Needs JDK 25.

```bash
cd 001-Restful-Web-Services
./gradlew bootRun    # http://localhost:8080/users, user admin / admin (API_USER / API_PASSWORD)
./gradlew build      # tests
```

The microservices are one multi-project Gradle build:

```bash
cd 002-Microservices
./gradlew build                                  # tests of every service
./gradlew :naming-server:bootRun                 # start the registry first, then the others
docker compose up --build                        # or everything at once
curl localhost:8765/convert/from/USD/to/EUR/quantity/10
```

## Certificate

[Udemy · Master Microservices with Spring Boot and Spring Cloud](Resources/Certificate.pdf)

## License

[MIT](LICENSE)
