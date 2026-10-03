# Postman Collection Guide

## Overview

The `Underwriting_API.postman_collection.json` file contains a complete set of API requests for testing the Underwriting API and the Experian mock infrastructure.

---

## Collection Structure

### **1. Customers**
Complete CRUD operations for customer management:
- Get All Customers
- Create Customer
- Get Customer by ID
- Update Customer (PATCH with JSON Patch)
- Delete Customer

### **2. Users**
Complete CRUD operations for user management:
- Get All Users
- Create User
- Get User by ID
- Update User (PATCH with JSON Patch)
- Delete User

### **3. Experian Mock API (WireMock)**
Test endpoints for the Experian credit score integration:
- Get Credit Score - High Score (800)
- Get Credit Score - Low Score (300)
- Get Credit Score - Business Not Found
- Get Credit Score - Service Unavailable (503)
- Get Credit Score - Internal Server Error (500)
- WireMock Admin - Get All Mappings
- WireMock Admin - Reset All Stubs

### **4. Health Check**
Application health monitoring:
- Application Health

---

## Environment Variables

The collection uses the following variables:

| Variable | Default Value | Description |
|----------|--------------|-------------|
| `base_url` | `http://localhost:8080/api` | Underwriting API base URL |
| `experian_base_url` | `http://localhost:8089` | Experian mock API base URL (WireMock) |
| `customer_id` | `123e4567-e89b-12d3-a456-426614174000` | Sample customer UUID |
| `user_id` | `456e7890-e12b-34d5-a678-901234567890` | Sample user UUID |

---

## Experian Mock API Usage

### **Test Scenarios**

The Experian mock API provides pre-configured responses based on the tax identifier:

| Tax Identifier | Scenario | Response |
|----------------|----------|----------|
| `12345678` | High credit score | 800 score, $150,000 recommended limit |
| `98765432` | Low credit score | 300 score, $0 recommended limit |
| `44444444` | Business not found | `success: false`, `result: null` |
| `11111111` | Timeout | Delays 31 seconds (exceeds client timeout) |
| `22222222` | Service unavailable | HTTP 503 error |
| `33333333` | Internal server error | HTTP 500 error |

### **Example Request**

```http
GET http://localhost:8089/api/v1/credit-score?taxIdentifier=12345678
Accept: application/json
```

### **Example Response (High Score)**

```json
{
  "requestId": "req-12345678-abcd-1234-5678-1234567890ab",
  "success": true,
  "result": {
    "businessHeader": {
      "bin": "123456789",
      "businessName": "Test Company Inc.",
      "address": "123 Main St, New York, NY 10001",
      "phone": "+1-555-123-4567",
      "taxId": "12345678",
      "websiteUrl": "https://testcompany.com",
      "legalBusinessName": "Test Company Incorporated",
      "dbaNames": ["Test Co", "TC Inc"]
    },
    "commercialScore": {
      "score": 800,
      "recommendedCreditLimitAmount": 150000
    }
  }
}
```

### **Example Response (Business Not Found)**

```json
{
  "requestId": "req-44444444-abcd-1234-5678-1234567890ab",
  "success": false,
  "result": null
}
```

---

## WireMock Admin Endpoints

### **Get All Mappings**

View all configured stub mappings:

```http
GET http://localhost:8089/__admin/mappings
Accept: application/json
```

This is useful for:
- Debugging stub configurations
- Verifying which stubs are active
- Understanding the mock API behavior

### **Reset All Stubs**

Reset all stub mappings to default state:

```http
POST http://localhost:8089/__admin/mappings/reset
Content-Type: application/json
```

This is useful for:
- Cleaning up test data between test runs
- Resetting to default stub configurations
- Ensuring clean test state

---

## Getting Started

### **1. Import Collection**

1. Open Postman
2. Click **Import**
3. Select `Underwriting_API.postman_collection.json`
4. Click **Import**

### **2. Start Services**

Using Docker Compose:

```bash
cd ratio-code-exercise
docker-compose up -d
```

This starts:
- **underwriting-api** on port 8080
- **postgres** on port 5432
- **experian-mock** (WireMock) on port 8089

### **3. Verify Services**

Check application health:
```bash
curl http://localhost:8080/api/actuator/health
```

Check WireMock is running:
```bash
curl http://localhost:8089/__admin/mappings
```

### **4. Test Experian Integration**

In Postman, run the "Get Credit Score - High Score" request:
- Should return HTTP 200
- Should show credit score of 800
- Should show recommended limit of $150,000

---

## Advanced Usage

### **Custom Stub Mappings**

You can create custom stub mappings using the WireMock admin API:

```bash
curl -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPath": "/api/v1/credit-score",
      "queryParameters": {
        "taxIdentifier": {
          "equalTo": "99999999"
        }
      }
    },
    "response": {
      "status": 200,
      "jsonBody": {
        "requestId": "custom-request",
        "success": true,
        "result": {
          "commercialScore": {
            "score": 750,
            "recommendedCreditLimitAmount": 100000
          }
        }
      }
    }
  }'
```

### **Verify Stub Was Called**

Check if a specific stub was invoked:

```bash
curl http://localhost:8089/__admin/requests
```

This returns all requests received by WireMock, useful for debugging.

---

## Troubleshooting

### **Experian Mock Not Responding**

1. Check if WireMock container is running:
   ```bash
   docker-compose ps experian-mock
   ```

2. Check WireMock logs:
   ```bash
   docker-compose logs experian-mock
   ```

3. Restart the service:
   ```bash
   docker-compose restart experian-mock
   ```

### **Wrong Port**

If using Docker Compose:
- **Internal** (from underwriting-api): `http://experian-mock:9090`
- **External** (from Postman/host): `http://localhost:8089`

The Postman collection uses `http://localhost:8089` for external access.

### **Stubs Not Working**

Reset all stubs to default:
```bash
curl -X POST http://localhost:8089/__admin/mappings/reset
```

Or restart the WireMock container:
```bash
docker-compose restart experian-mock
```

---

## Response Examples

### **High Credit Score (800)**

```json
{
  "requestId": "req-12345678-abcd-1234-5678-1234567890ab",
  "success": true,
  "result": {
    "businessHeader": {
      "bin": "123456789",
      "businessName": "Test Company Inc.",
      "address": "123 Main St, New York, NY 10001",
      "phone": "+1-555-123-4567",
      "taxId": "12345678",
      "websiteUrl": "https://testcompany.com",
      "legalBusinessName": "Test Company Incorporated",
      "dbaNames": ["Test Co", "TC Inc"]
    },
    "commercialScore": {
      "score": 800,
      "recommendedCreditLimitAmount": 150000
    }
  }
}
```

### **Low Credit Score (300)**

```json
{
  "requestId": "req-98765432-abcd-1234-5678-1234567890ab",
  "success": true,
  "result": {
    "businessHeader": {
      "bin": "987654321",
      "businessName": "Risky Business LLC",
      "address": "456 Risk Ave, Chicago, IL 60601",
      "phone": "+1-555-987-6543",
      "taxId": "98765432",
      "websiteUrl": "https://riskybusiness.com",
      "legalBusinessName": "Risky Business Limited Liability Company",
      "dbaNames": []
    },
    "commercialScore": {
      "score": 300,
      "recommendedCreditLimitAmount": 0
    }
  }
}
```

### **Business Not Found**

```json
{
  "requestId": "req-44444444-abcd-1234-5678-1234567890ab",
  "success": false,
  "result": null
}
```

### **Service Unavailable (503)**

```json
{
  "error": "Service temporarily unavailable"
}
```

### **Internal Server Error (500)**

```json
{
  "error": "Internal server error from Experian"
}
```

---

## Summary

The Postman collection provides:

✅ **Complete API coverage** for Customers and Users  
✅ **Experian mock API** with 6 test scenarios  
✅ **WireMock admin endpoints** for debugging  
✅ **Pre-configured variables** for easy testing  
✅ **Example responses** for all scenarios  

This collection allows you to:
1. Test all available API endpoints
2. Understand the Experian API contract and responses
3. Debug integration issues using WireMock admin endpoints
4. Verify error handling with different test scenarios

The mock infrastructure provides a complete testing environment without needing access to the real Experian API.
