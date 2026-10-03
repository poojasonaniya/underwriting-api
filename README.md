# Underwriting API - Coding Exercise

Welcome! This is a Spring Boot microservice for underwriting operations. This README will guide you through setting up your development environment and understanding the project structure.

> **📖 Important**: Please read all the documentation files in this project carefully:
> - **README.md** (this file) - Setup instructions and project structure
> - **POSTMAN_COLLECTION.md** - API testing guide and endpoint documentation
> - **wiremock/README.md** - Experian mock API documentation and test scenarios
> 
> These files contain essential information about the project architecture, available endpoints, and testing tools.

## Table of Contents

- [Required Task Before Interview](#required-task-before-interview)
- [Live Coding Session (During Interview)](#live-coding-session-during-interview)
- [Prerequisites](#prerequisites)
- [Quick Start Guide](#quick-start-guide)
- [Project Structure](#project-structure)
- [Running Tests](#running-tests)
- [Example: Adding a New Feature](#example-adding-a-new-feature)
- [API Testing with Postman](#api-testing-with-postman)
- [Troubleshooting](#troubleshooting)

## Required Task Before Interview

**⚠️ Important**: Before your interview, you must complete the following task to demonstrate your understanding of the codebase and Spring Boot development.

### Task: Implement a Paginated Endpoint

Create a new REST endpoint that returns **paginated data** for either **Users** or **Customers** (your choice). This task allows you to work in the language you're most comfortable with:

- **Java**: Implement pagination for Users (following `UserController.java` patterns)
- **Kotlin**: Implement pagination for Customers (following `CustomerController.kt` patterns)
- **Both**: If you're comfortable with both languages, feel free to implement both!

### Requirements

Your endpoint must:

1. **Return paginated results** using Spring Data's pagination support
2. **Accept query parameters** for pagination control:
   - `page` (integer, default: 0) - Page number (zero-indexed)
   - `size` (integer, default: 20) - Number of items per page
   - `sort` (string, optional) - Sort field and direction (e.g., `firstName,asc` or `createdDate,desc`)

3. **Return proper response structure**:
   ```json
   {
     "content": [ /* array of users or customers */ ],
     "page": {
       "size": 20,
       "number": 0,
       "totalElements": 100,
       "totalPages": 5
     }
   }
   ```

4. **Include integration tests** that verify:
   - Pagination works correctly (first page, last page, middle page)
   - Page size parameter is respected
   - Sorting works correctly
   - Empty results are handled properly

### What We're Looking For

- ✅ **Correct Spring Data pagination** usage
- ✅ **Proper parameter handling** and validation
- ✅ **Comprehensive integration tests** covering edge cases
- ✅ **Code consistency** with existing patterns in the codebase
- ✅ **Proper error handling** for invalid parameters
- ✅ **Documentation** (KDoc/JavaDoc) for new methods

### Submission

Please ensure your implementation is complete and all tests pass before your interview. We'll review your code together and discuss your design decisions during the interview.

---

## Live Coding Session (During Interview)

**⚠️ Important**: During the interview, you will be asked to complete a **live coding exercise** that builds upon this codebase.

### What to Expect

The live coding session will involve:

- **Integrating with the Experian API client** (`ExperianClient.kt` / `ExperianService.kt`)
- **Working with the existing domain models** (Customers, Users, or both)
- **Implementing business logic** that leverages external API calls
- **Handling API responses** including success cases, errors, and edge cases

### How to Prepare

1. **Familiarize yourself with the Experian client code:**
   - Review `src/main/kotlin/com/ratiotech/underwriting/api/clients/experian/`
   - Understand how `ExperianClient.kt` and `ExperianService.kt` work
   - Study the request/response models in the `models/` subdirectory

2. **Understand the WireMock test scenarios:**
   - Read [wiremock/README.md](./wiremock/README.md) thoroughly
   - Know the different tax IDs and their expected responses
   - Understand how to test different scenarios (success, errors, timeouts)

3. **Review the test infrastructure:**
   - Study `ExperianWireMockContainer.kt` and `ExperianStubs.kt` in the test directory
   - Understand how integration tests mock external API calls

4. **Be comfortable with:**
   - Making HTTP calls to external services in Spring Boot
   - Error handling for external API failures
   - Writing integration tests that use WireMock stubs

> **💡 Tip**: The better you understand the Experian integration patterns in this codebase, the smoother your live coding session will be!

---

## Prerequisites

Before you begin, ensure you have the following installed:

- **Docker** and **Docker Compose** (recommended for quick setup)
- **Java 17** or higher (if running locally without Docker)
- **Gradle 8.5+** (if running locally without Docker)
- **IntelliJ IDEA** (Community or Ultimate edition) - Recommended IDE for development
- **Postman** (optional, for API testing)

### Port Requirements

The following ports must be available:
- **8080**: Underwriting API
- **8081**: Debug port (optional)
- **5432**: PostgreSQL database
- **8089**: Experian Mock API (WireMock)

## Quick Start Guide

### Option 1: Using Docker Compose (Recommended)

1. **Extract the project files:**
   ```bash
   # Extract the zip file you received
   unzip ratio-code-exercise.zip
   cd ratio-code-exercise
   ```

2. **Copy environment configuration:**
   ```bash
   cp .env.example .env
   ```
   
   > **Note**: The default configuration stores PostgreSQL data in `./postgres-data` inside your project directory. This works on all operating systems and requires no additional configuration. If you prefer to use a different location, you can edit the `.env` file and change the `POSTGRES_DATA_PATH` variable.

3. **Start all services:**
   ```bash
   docker-compose up -d
   ```
   
   This will start:
   - **underwriting-api** on port 8080
   - **postgres** database on port 5432
   - **experian-mock** (WireMock) on port 8089

4. **Verify the services are running:**
   ```bash
   # Check application health
   curl http://localhost:8080/api/actuator/health
   
   # Check WireMock is running
   curl http://localhost:8089/__admin/mappings
   ```

5. **Understanding the Experian Mock API:**
   
   **What is Experian?** Experian is a global credit reporting agency that provides credit scores and business credit information. In real-world applications, businesses use Experian's API to assess the creditworthiness of customers before extending credit or financing.
   
   The project includes a **WireMock server** that simulates the Experian credit score API. This mock service provides:
   - **Credit score checks** based on business tax identifiers
   - **Pre-configured test scenarios** (high scores, low scores, errors, timeouts)
   - **Different response types** for testing various business cases
   
   The mock API allows you to test credit check integrations without connecting to the real Experian service. It returns different responses based on the tax identifier you provide:
   - Tax ID `12345678` → High credit score (800)
   - Tax ID `98765432` → Low credit score (300)
   - Tax ID `44444444` → Business not found
   - Tax ID `22222222` → Service unavailable error
   - And more scenarios...
   
   **For complete documentation**, see [wiremock/README.md](./wiremock/README.md) for all available test scenarios and response formats.

6. **View application logs:**
   ```bash
   docker-compose logs -f underwriting-api
   ```

### Option 2: Running Locally (Without Docker)

1. **Start PostgreSQL and WireMock using Docker Compose:**
   ```bash
   docker-compose up -d postgres experian-mock
   ```

2. **Set environment variables:**
   ```bash
   export POSTGRES_URL="jdbc:postgresql://localhost:5432/underwriting"
   export POSTGRES_USERNAME="underwriting_user"
   export POSTGRES_PASSWORD="test123"
   export EXPERIAN_BASE_URL="http://localhost:8089"
   export SPRING_PROFILES_ACTIVE="local"
   ```

3. **Run the application:**
   
   **Option A: Using Gradle Command Line**
   ```bash
   ./gradlew bootRun
   ```
   
   **Option B: Using IntelliJ IDEA (Recommended)**
   
   1. **Open the project in IntelliJ IDEA:**
      - File → Open → Select the `ratio-code-exercise` directory
      - IntelliJ will automatically detect it as a Gradle project
   
   2. **Configure environment variables:**
      - Open `UnderwritingApiApplication.kt` (located in `src/main/kotlin/com/ratiotech/underwriting/api/`)
      - Click the green play button (▶) next to the `main` function
      - Select "Modify Run Configuration..."
      - In the "Environment variables" field, add:
        ```
        POSTGRES_URL=jdbc:postgresql://localhost:5432/underwriting;POSTGRES_USERNAME=underwriting_user;POSTGRES_PASSWORD=test123;EXPERIAN_BASE_URL=http://localhost:8089;SPRING_PROFILES_ACTIVE=local
        ```
      - Click "Apply" and "OK"
   
   3. **Run the application:**
      - Click the green play button (▶) next to the `main` function
      - Or use the keyboard shortcut: `Ctrl+Shift+F10` (Windows/Linux) or `Ctrl+Shift+R` (macOS)
   
   4. **Debug the application (optional):**
      - Click the debug button (🐞) instead of the play button
      - Set breakpoints by clicking in the left margin of the code editor
      - The application will pause at breakpoints, allowing you to inspect variables
   
   **IntelliJ IDEA Tips:**
   - **Auto-import dependencies**: IntelliJ will prompt you to import Gradle changes when you modify `build.gradle.kts`
   - **Run tests**: Right-click on any test class or method and select "Run" or "Debug"
   - **Database tool**: IntelliJ Ultimate includes a database tool to connect to PostgreSQL directly
   - **Kotlin support**: Both Community and Ultimate editions have excellent Kotlin support built-in

4. **Access the application:**
   - Health Check: http://localhost:8080/api/actuator/health
   - API Base URL: http://localhost:8080/api

## Project Structure

The project follows a layered architecture pattern with clear separation of concerns:

```
ratio-code-exercise/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ratiotech/underwriting/api/
│   │   │       ├── controllers/          # REST endpoints (Java)
│   │   │       │   ├── UserController.java
│   │   │       │   ├── requests/         # Request DTOs
│   │   │       │   └── responses/        # Response DTOs
│   │   │       ├── entities/             # JPA entities
│   │   │       │   └── UserEntity.java
│   │   │       ├── logic/                # Business logic
│   │   │       │   ├── UserLogic.java
│   │   │       │   └── translators/      # Entity ↔ DTO converters
│   │   │       └── repositories/         # JPA repositories
│   │   │           └── UserRepository.java
│   │   │
│   │   ├── kotlin/
│   │   │   └── com/ratiotech/underwriting/api/
│   │   │       ├── UnderwritingApiApplication.kt  # Main application entry point
│   │   │       ├── clients/              # External API clients
│   │   │       │   └── experian/
│   │   │       │       ├── ExperianClient.kt
│   │   │       │       ├── ExperianService.kt
│   │   │       │       └── models/       # Client DTOs
│   │   │       ├── controllers/          # REST endpoints (Kotlin)
│   │   │       │   ├── CustomerController.kt
│   │   │       │   ├── requests/         # Request DTOs
│   │   │       │   └── responses/        # Response DTOs
│   │   │       ├── entities/             # JPA entities
│   │   │       │   └── CustomerEntity.kt
│   │   │       ├── enums/                # Application enums
│   │   │       ├── logic/                # Business logic
│   │   │       │   ├── CustomerLogic.kt
│   │   │       │   └── translators/      # Entity ↔ DTO converters
│   │   │       ├── repositories/         # JPA repositories
│   │   │       │   └── CustomerRepository.kt
│   │   │       └── shared/               # Shared utilities
│   │   │           ├── advice/           # Exception handlers
│   │   │           ├── constants/        # Application constants
│   │   │           ├── exceptions/       # Custom exceptions
│   │   │           └── logic/            # Shared business logic
│   │   │
│   │   └── resources/
│   │       ├── application.yaml          # Main application configuration
│   │       ├── application-compose.yaml  # Docker Compose profile
│   │       ├── application-local.yaml    # Local development profile
│   │       └── db/migration/             # Flyway database migrations
│   │           ├── V1.0__create_customers_table.sql
│   │           ├── V2.0__create_users_table.sql
│   │           └── V3.0__add_indexes.sql
│   │
│   └── test/
│       └── kotlin/
│           └── com/ratiotech/underwriting/api/
│               ├── clients/               # Unit tests for clients (empty)
│               ├── controllers/           # Integration tests for endpoints
│               │   └── CustomerControllerIntegrationTest.kt
│               ├── logic/                 # Unit tests for business logic
│               │   └── translators/
│               └── shared/                # Test utilities
│                   ├── IntegrationTestBase.kt
│                   └── experian/
│                       ├── ExperianWireMockContainer.kt
│                       └── ExperianStubs.kt
│
├── wiremock/                             # WireMock stub configurations
│   ├── mappings/                         # API stub definitions
│   │   ├── credit-score-success.json
│   │   ├── credit-score-declined.json
│   │   └── ...
│   ├── __files/                          # Response templates
│   └── README.md                         # WireMock documentation
│
├── docker-compose.yaml                   # Docker services configuration
├── Dockerfile                            # Application container definition
├── build.gradle.kts                      # Gradle build configuration
├── .env.example                          # Environment variables template
├── .gitignore                            # Git ignore rules
├── POSTMAN_COLLECTION.md                 # Postman API testing guide
├── Underwriting_API.postman_collection.json  # Postman collection
└── README.md                             # This file
```

### Key Components

#### Controllers Layer
- **Purpose**: Handle HTTP requests and responses
- **Location**: `src/main/*/com/ratiotech/underwriting/api/controllers/`
- **Responsibilities**:
  - Define REST endpoints with `@GetMapping`, `@PostMapping`, etc.
  - Validate request data using `@Valid`
  - Delegate business logic to the Logic layer
  - Return proper HTTP status codes

#### Logic Layer
- **Purpose**: Implement business logic and rules
- **Location**: `src/main/kotlin/com/ratiotech/underwriting/api/logic/`
- **Responsibilities**:
  - Process business operations
  - Orchestrate data access through repositories
  - Transform entities to DTOs using translators
  - Handle transactional operations with `@Transactional`

#### Entities Layer
- **Purpose**: Define database table structures
- **Location**: `src/main/*/com/ratiotech/underwriting/api/entities/`
- **Responsibilities**:
  - Map to database tables using JPA annotations
  - Define relationships between entities
  - Include audit fields (createdDate, createdBy, etc.)

#### Repositories Layer
- **Purpose**: Handle database operations
- **Location**: `src/main/*/com/ratiotech/underwriting/api/repositories/`
- **Responsibilities**:
  - Extend `JpaRepository` for CRUD operations
  - Define custom query methods
  - Provide data access abstraction

#### Database Migrations
- **Purpose**: Version-controlled database schema changes
- **Location**: `src/main/resources/db/migration/`
- **Tool**: Flyway
- **Naming**: `V{version}__{description}.sql` (e.g., `V1.0__create_customers_table.sql`)

## Running Tests

### Running All Tests

```bash
# Using Gradle
./gradlew test
```

### Running Specific Tests

```bash
# Run tests for a specific class
./gradlew test --tests "CustomerControllerIntegrationTest"

# Run a specific test method
./gradlew test --tests "CustomerControllerIntegrationTest.shouldCreateCustomerSuccessfully"

# Run all tests in a package
./gradlew test --tests "com.ratiotech.underwriting.api.controllers.*"
```

### Test Structure

- **Integration Tests**: Test complete request-response cycles with real database
  - Located in: `src/test/kotlin/com/ratiotech/underwriting/api/controllers/`
  - Use `IntegrationTestBase` for setup
  - Use Testcontainers for PostgreSQL and WireMock
  
- **Unit Tests**: Test individual components in isolation
  - Located in: `src/test/kotlin/com/ratiotech/underwriting/api/logic/`
  - Use MockK for mocking dependencies


## Example: Adding a New Feature

Let's walk through adding a **Sellers** feature with a complete CRUD API. This example demonstrates the typical development workflow.

### Step 1: Create Database Migration

Create a new migration file: `src/main/resources/db/migration/V5.0__create_sellers_table.sql`

```sql
CREATE TABLE IF NOT EXISTS sellers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(50),
    tax_id VARCHAR(50) NOT NULL UNIQUE,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_by UUID NOT NULL
);

CREATE INDEX idx_sellers_tax_id ON sellers(tax_id);
CREATE INDEX idx_sellers_email ON sellers(contact_email);
```

### Step 2: Create Entity

Create: `src/main/kotlin/com/ratiotech/underwriting/api/entities/SellerEntity.kt`

```kotlin
package com.ratiotech.underwriting.api.entities

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "sellers")
@EntityListeners(AuditingEntityListener::class)
data class SellerEntity(
  @Column(name = "company_name", nullable = false)
  var companyName: String,

  @Column(name = "contact_email", nullable = false, unique = true)
  var contactEmail: String,

  @Column(name = "phone_number")
  var phoneNumber: String? = null,

  @Column(name = "tax_id", nullable = false, unique = true)
  var taxId: String,

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private val id: UUID? = null,

  @CreatedDate
  @Column(name = "created_date", nullable = false, updatable = false)
  var createdDate: Instant? = null,

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false)
  var createdBy: UUID? = null,

  @LastModifiedDate
  @Column(name = "last_modified_date", nullable = false)
  var lastModifiedDate: Instant? = null,

  @LastModifiedBy
  @Column(name = "last_modified_by", nullable = false)
  var lastModifiedBy: UUID? = null
) {
  fun getId(): UUID? = id
}
```

### Step 3: Create Repository

Create: `src/main/kotlin/com/ratiotech/underwriting/api/repositories/SellerRepository.kt`

```kotlin
package com.ratiotech.underwriting.api.repositories

import com.ratiotech.underwriting.api.entities.SellerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SellerRepository : JpaRepository<SellerEntity, UUID> {
  fun existsByTaxId(taxId: String): Boolean
  fun existsByContactEmail(contactEmail: String): Boolean
}
```

### Step 4: Create DTOs

Create request DTO: `src/main/kotlin/com/ratiotech/underwriting/api/controllers/requests/CreateSellerRequest.kt`

```kotlin
package com.ratiotech.underwriting.api.controllers.requests

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateSellerRequest(
  @field:NotBlank(message = "Company name is required")
  val companyName: String,

  @field:NotBlank(message = "Contact email is required")
  @field:Email(message = "Invalid email format")
  val contactEmail: String,

  val phoneNumber: String? = null,

  @field:NotBlank(message = "Tax ID is required")
  val taxId: String
)
```

Create response DTO: `src/main/kotlin/com/ratiotech/underwriting/api/controllers/responses/Seller.kt`

```kotlin
package com.ratiotech.underwriting.api.controllers.responses

import java.time.Instant
import java.util.UUID

data class Seller(
  val id: UUID,
  val companyName: String,
  val contactEmail: String,
  val phoneNumber: String?,
  val taxId: String,
  val createdDate: Instant,
  val createdBy: UUID,
  val lastModifiedDate: Instant,
  val lastModifiedBy: UUID
)
```

### Step 5: Create Translator

Create: `src/main/kotlin/com/ratiotech/underwriting/api/logic/translators/SellerTranslator.kt`

```kotlin
package com.ratiotech.underwriting.api.logic.translators

import com.ratiotech.underwriting.api.controllers.responses.Seller
import com.ratiotech.underwriting.api.entities.SellerEntity
import org.springframework.stereotype.Component

@Component
class SellerTranslator {
  
  fun toModel(entity: SellerEntity): Seller {
    return Seller(
      id = entity.getId()!!,
      companyName = entity.companyName,
      contactEmail = entity.contactEmail,
      phoneNumber = entity.phoneNumber,
      taxId = entity.taxId,
      createdDate = entity.createdDate!!,
      createdBy = entity.createdBy!!,
      lastModifiedDate = entity.lastModifiedDate!!,
      lastModifiedBy = entity.lastModifiedBy!!
    )
  }
}
```

### Step 6: Create Business Logic

Create: `src/main/kotlin/com/ratiotech/underwriting/api/logic/SellerLogic.kt`

```kotlin
package com.ratiotech.underwriting.api.logic

import com.ratiotech.underwriting.api.controllers.requests.CreateSellerRequest
import com.ratiotech.underwriting.api.controllers.responses.Seller
import com.ratiotech.underwriting.api.entities.SellerEntity
import com.ratiotech.underwriting.api.logic.translators.SellerTranslator
import com.ratiotech.underwriting.api.repositories.SellerRepository
import com.ratiotech.underwriting.api.shared.constants.Constants.SYSTEM_IDENTITY
import com.ratiotech.underwriting.api.shared.exceptions.BadRequestException
import com.ratiotech.underwriting.api.shared.exceptions.NotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class SellerLogic(
  private val sellerRepository: SellerRepository,
  private val sellerTranslator: SellerTranslator
) {

  @Transactional
  fun createSeller(request: CreateSellerRequest): UUID {
    if (sellerRepository.existsByTaxId(request.taxId)) {
      throw BadRequestException("Seller with tax ID '${request.taxId}' already exists")
    }

    val sellerEntity = SellerEntity(
      companyName = request.companyName,
      contactEmail = request.contactEmail,
      phoneNumber = request.phoneNumber,
      taxId = request.taxId,
      createdBy = SYSTEM_IDENTITY,
      lastModifiedBy = SYSTEM_IDENTITY
    )

    val savedSeller = sellerRepository.save(sellerEntity)
    return savedSeller.getId()!!
  }

  @Transactional(readOnly = true)
  fun getAllSellers(): List<Seller> {
    return sellerRepository.findAll().map { sellerTranslator.toModel(it) }
  }

  @Transactional(readOnly = true)
  fun getSellerById(sellerId: UUID): Seller {
    val sellerEntity = sellerRepository.findById(sellerId).orElseThrow {
      NotFoundException("Seller not found with id: $sellerId")
    }
    return sellerTranslator.toModel(sellerEntity)
  }

  @Transactional
  fun deleteSeller(sellerId: UUID) {
    if (!sellerRepository.existsById(sellerId)) {
      throw NotFoundException("Seller not found with id: $sellerId")
    }
    sellerRepository.deleteById(sellerId)
  }
}
```

### Step 7: Create Controller

Create: `src/main/kotlin/com/ratiotech/underwriting/api/controllers/SellerController.kt`

```kotlin
package com.ratiotech.underwriting.api.controllers

import com.ratiotech.underwriting.api.controllers.requests.CreateSellerRequest
import com.ratiotech.underwriting.api.controllers.responses.Seller
import com.ratiotech.underwriting.api.logic.SellerLogic
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
class SellerController(private val sellerLogic: SellerLogic) {

  @GetMapping("/v1/sellers")
  fun getAllSellers(): ResponseEntity<List<Seller>> {
    val sellers = sellerLogic.getAllSellers()
    return ResponseEntity.ok(sellers)
  }

  @PostMapping("/v1/sellers")
  fun createSeller(@Valid @RequestBody request: CreateSellerRequest): ResponseEntity<Void> {
    val sellerId = sellerLogic.createSeller(request)
    val location = ServletUriComponentsBuilder
      .fromCurrentRequest()
      .path("/{id}")
      .buildAndExpand(sellerId)
      .toUri()
    return ResponseEntity.created(location).build()
  }

  @GetMapping("/v1/sellers/{id}")
  fun getSellerById(@PathVariable id: UUID): ResponseEntity<Seller> {
    val seller = sellerLogic.getSellerById(id)
    return ResponseEntity.ok(seller)
  }

  @DeleteMapping("/v1/sellers/{id}")
  fun deleteSeller(@PathVariable id: UUID): ResponseEntity<Void> {
    sellerLogic.deleteSeller(id)
    return ResponseEntity.noContent().build()
  }
}
```

### Step 8: Create Integration Tests

Create: `src/test/kotlin/com/ratiotech/underwriting/api/controllers/SellerControllerIntegrationTest.kt`

```kotlin
package com.ratiotech.underwriting.api.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.ratiotech.underwriting.api.controllers.requests.CreateSellerRequest
import com.ratiotech.underwriting.api.shared.IntegrationTestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

class SellerControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  override fun beforeEach() {}

  @Test
  fun `should create seller successfully`() {
    val request = CreateSellerRequest(
      companyName = "Test Company LLC",
      contactEmail = "contact@testcompany.com",
      phoneNumber = "555-1234",
      taxId = "12-3456789"
    )

    mockMvc.perform(
      post("/v1/sellers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request))
    )
      .andExpect(status().isCreated)
      .andExpect(header().exists("Location"))
  }

  @Test
  fun `should get all sellers`() {
    mockMvc.perform(get("/v1/sellers"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$").isArray)
  }
}
```

### Step 9: Run and Test

1. **Restart the application** to apply database migrations:
   ```bash
   docker-compose restart underwriting-api
   ```

2. **Run the tests**:
   ```bash
   ./gradlew test --tests "SellerControllerIntegrationTest"
   ```

3. **Test the API manually**:
   ```bash
   # Create a seller
   curl -X POST http://localhost:8080/api/v1/sellers \
     -H "Content-Type: application/json" \
     -d '{
       "companyName": "Acme Corp",
       "contactEmail": "sales@acme.com",
       "phoneNumber": "555-0100",
       "taxId": "98-7654321"
     }'

   # Get all sellers
   curl http://localhost:8080/api/v1/sellers
   ```

### Summary of Files Created

For a complete CRUD feature, you typically create:
- **1 Migration file**: Database schema
- **1 Entity**: Database model
- **1 Repository**: Data access
- **2-3 DTOs**: Request and response models
- **1 Translator**: Entity ↔ DTO conversion
- **1 Logic**: Business logic
- **1 Controller**: REST endpoints
- **1 Integration Test**: End-to-end testing

This pattern is consistent across the codebase and follows Ratiotech's architecture standards.

## API Testing with Postman

A comprehensive Postman collection is included to test all API endpoints.

### Import the Collection

1. **Open Postman**
2. **Import the collection file:**
   ```
   Underwriting_API.postman_collection.json
   ```
3. **The collection includes:**
   - **Customer Management**: 5 endpoints (GET all, POST create, GET by ID, PATCH update, DELETE)
   - **User Management**: 5 endpoints (GET all, POST create, GET by ID, PATCH update, DELETE)
   - **Health Check**: Application status endpoint
   - **Pre-configured variables**: Base URL and sample UUIDs
   - **Request examples**: Complete with proper headers and body data
   - **Response examples**: Expected responses for each endpoint

### Available Endpoints

**Customers:**
- `GET /v1/customers` - Get all customers
- `POST /v1/customers` - Create new customer
- `GET /v1/customers/{id}` - Get customer by ID
- `PATCH /v1/customers/{id}` - Update customer (JSON Patch)
- `DELETE /v1/customers/{id}` - Delete customer

**Users:**
- `GET /v1/users` - Get all users
- `POST /v1/users` - Create new user
- `GET /v1/users/{id}` - Get user by ID
- `PATCH /v1/users/{id}` - Update user (JSON Patch)
- `DELETE /v1/users/{id}` - Delete user

**Health:**
- `GET /actuator/health` - Application health check

### Environment Variables

The collection includes these pre-configured variables:
- `base_url`: http://localhost:8080/api
- `customer_id`: Sample customer UUID for testing
- `user_id`: Sample user UUID for testing

## Troubleshooting

### Application Won't Start

1. **Check if ports are available:**
   ```bash
   # Check if port 8080 is in use
   lsof -i :8080
   
   # Check if port 5432 is in use
   lsof -i :5432
   
   # Check if port 8089 is in use
   lsof -i :8089
   ```

2. **View application logs:**
   ```bash
   docker-compose logs underwriting-api
   ```

3. **Rebuild from scratch:**
   ```bash
   docker-compose down -v
   docker-compose up --build -d
   ```

### Database Issues

1. **Reset database:**
   ```bash
   docker-compose down -v
   docker-compose up -d postgres
   docker-compose up -d underwriting-api
   ```

2. **Access database directly:**
   ```bash
   docker-compose exec postgres psql -U underwriting_user -d underwriting
   ```

3. **Check database logs:**
   ```bash
   docker-compose logs postgres
   ```

### Tests Failing

1. **Clean and rebuild:**
   ```bash
   ./gradlew clean build
   ```

2. **Run tests with detailed output:**
   ```bash
   ./gradlew test --info
   ```

3. **Check test reports:**
   ```bash
   open build/reports/tests/test/index.html
   ```

### WireMock Issues

1. **Verify WireMock is running:**
   ```bash
   curl http://localhost:8089/__admin/mappings
   ```

2. **Reset WireMock stubs:**
   ```bash
   curl -X POST http://localhost:8089/__admin/mappings/reset
   ```

3. **View WireMock logs:**
   ```bash
   docker-compose logs experian-mock
   ```

### Docker Compose Commands

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Rebuild and restart
docker-compose up --build -d

# View logs for specific service
docker-compose logs -f underwriting-api

# View all logs
docker-compose logs -f

# Restart a specific service
docker-compose restart underwriting-api

# Stop and remove all containers, networks, and volumes
docker-compose down -v

# Check service status
docker-compose ps
```

### Common Issues

**Issue**: `org.postgresql.util.PSQLException: FATAL: database "underwriting" does not exist`
- **Solution**: The database is created automatically on first run. Try: `docker-compose down -v && docker-compose up -d`

**Issue**: `Address already in use` error
- **Solution**: Another service is using the port. Stop the conflicting service or change the port in `docker-compose.yaml`

**Issue**: Permission denied errors on macOS/Linux
- **Solution**: Ensure your user has Docker permissions or use `sudo` for Docker commands

**Issue**: Tests fail with `Could not find a valid Docker environment` or hang on startup
- **Solution**: Create `~/.docker-java.properties` with `api.version=1.44` and re-run the tests

**Issue**: Tests pass locally but fail in Docker
- **Solution**: Ensure you're using the correct Spring profile. Check `SPRING_PROFILES_ACTIVE` environment variable

## Additional Resources

- **Postman Collection**: See [POSTMAN_COLLECTION.md](./POSTMAN_COLLECTION.md) for detailed API testing guide
- **WireMock Documentation**: See [wiremock/README.md](./wiremock/README.md) for Experian API mock details
- **Spring Boot Actuator**: http://localhost:8080/api/actuator for application monitoring

## Getting Help

If you encounter issues during setup or have questions:
1. Check the logs using `docker-compose logs`
2. Review the [Troubleshooting](#troubleshooting) section above
3. Verify all prerequisites are installed correctly
4. Ensure all required ports are available

---

**Good luck with the coding exercise! 🚀**
