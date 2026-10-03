# Experian Mock API (WireMock)

This directory contains WireMock configuration for simulating the Experian Credit Score API. The mock API is used for both manual testing (via Docker Compose) and automated integration testing (via Testcontainers).

## Overview

The Experian mock API simulates credit score checks for businesses using their tax identifier. It provides various test scenarios including successful responses, errors, timeouts, and edge cases.

## Customer Underwriting Process

**Underwriting** is the process of evaluating the creditworthiness and financial risk of a potential customer (business) before extending credit or financial services to them. In this application, the underwriting process involves:

### What is Underwriting?

Underwriting is a risk assessment process where we:
1. **Verify Business Identity**: Confirm the business exists and is legitimate
2. **Assess Credit Risk**: Evaluate the business's ability to repay debts
3. **Determine Credit Limits**: Calculate appropriate credit limits based on financial health
4. **Make Approval Decisions**: Approve, decline, or conditionally approve credit applications

### Experian's Role in Underwriting

Experian is a credit reporting agency that provides business credit scores and reports. During customer underwriting, we:

1. **Collect Business Information**: Gather the customer's tax identifier (EIN/TIN)
2. **Query Experian API**: Request credit score and business information
3. **Analyze Credit Score**: Evaluate the score (typically 300-850 range):
   - **750-850**: Excellent credit - Low risk, high credit limits
   - **650-749**: Good credit - Moderate risk, standard credit limits
   - **550-649**: Fair credit - Higher risk, lower credit limits
   - **300-549**: Poor credit - High risk, may decline or require additional security
4. **Make Underwriting Decision**: Approve/decline based on credit score and business data
5. **Set Credit Terms**: Determine credit limit, interest rates, and payment terms

### Example Underwriting Workflow

```
Customer Application → Collect Tax ID → Query Experian API → Receive Credit Score
                                                                      ↓
                                                            Analyze Score & Business Data
                                                                      ↓
                                                    ┌─────────────────┴─────────────────┐
                                                    ↓                                   ↓
                                            Score ≥ 650                            Score < 650
                                                    ↓                                   ↓
                                        Approve with Credit Limit          Decline or Request Additional Info
```

## Docker Compose Usage

### Starting the Mock API

The Experian mock API is automatically started when you run:

```bash
docker-compose up -d
```

The mock API will be available at:
- **Base URL**: `http://localhost:8089`
- **Admin UI**: `http://localhost:8089/__admin` (for managing stubs)

### Stopping the Mock API

```bash
docker-compose down
```

## API Specification

### Endpoint

**GET** `/api/v1/credit-score`

### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| taxIdentifier | string | Yes | The business tax identifier number |

### Response Format

```json
{
  "requestId": "string (UUID)",
  "success": boolean,
  "result": {
    "businessHeader": {
      "bin": "string",
      "businessName": "string",
      "address": "string",
      "phone": "string",
      "taxId": "string",
      "websiteUrl": "string",
      "legalBusinessName": "string",
      "dbaNames": ["string"]
    },
    "commercialScore": {
      "score": number (300-850),
      "recommendedCreditLimitAmount": number
    }
  }
}
```

## Test Scenarios

The following test scenarios are pre-configured with specific tax identifiers:

### 1. Successful Credit Check (Default)

**Tax Identifier**: Any numeric value not matching other scenarios

**Example**: `12345678`

**Response**: HTTP 200 with credit score 750 and recommended limit $100,000

```bash
curl "http://localhost:8089/api/v1/credit-score?taxIdentifier=12345678"
```

### 2. Low Credit Score

**Tax Identifier**: `99999999`

**Response**: HTTP 200 with credit score 300 (high risk) and $0 recommended limit

```bash
curl "http://localhost:8089/api/v1/credit-score?taxIdentifier=99999999"
```

### 3. Business Not Found

**Tax Identifier**: `00000000`

**Response**: HTTP 200 with `success: false` and `result: null`

```bash
curl "http://localhost:8089/api/v1/credit-score?taxIdentifier=00000000"
```

### 4. Service Unavailable (503)

**Tax Identifier**: `66666666`

**Response**: HTTP 503 with error message

```bash
curl "http://localhost:8089/api/v1/credit-score?taxIdentifier=66666666"
```

### 5. Timeout Scenario

**Tax Identifier**: `77777777`

**Response**: HTTP 200 after 31 second delay (simulates timeout)

```bash
curl "http://localhost:8089/api/v1/credit-score?taxIdentifier=77777777"
```

### 6. Internal Server Error

**Tax Identifier**: `88888888`

**Response**: HTTP 500 with error message

```bash
curl "http://localhost:8089/api/v1/credit-score?taxIdentifier=88888888"
```

## Integration Testing

Integration tests automatically load the same static mappings from this directory, providing default test scenarios without explicit stubbing.

### Default Mappings Available in Tests

All integration tests have access to the pre-configured scenarios:

```kotlin
class YourIntegrationTest : IntegrationTestBase() {
    
    @Test
    fun shouldCheckCreditScoreWithDefaultMapping() {
        // No explicit stubbing needed!
        // Tax identifier 99999999 automatically returns low credit score
        val response = experianClient.getCreditScore("99999999")
        
        assertEquals(300, response.result?.commercialScore?.score)
    }
    
    @Test
    fun shouldHandleBusinessNotFound() {
        // Tax identifier 00000000 automatically returns not found
        val response = experianClient.getCreditScore("00000000")
        
        assertEquals(false, response.success)
        assertNull(response.result)
    }
}
```

### Using ExperianStubs Helper for Custom Scenarios

For test-specific scenarios, use the ExperianStubs helper to override or add stubs:

```kotlin
import com.ratiotech.underwriting.api.shared.experian.ExperianStubs

class YourIntegrationTest : IntegrationTestBase() {
    
    @Test
    fun shouldCheckCreditScoreWithCustomStub() {
        // Override default behavior for specific test
        ExperianStubs.stubSuccessfulCreditScore(
            container = experianMockContainer,
            taxIdentifier = "12345678",
            creditScore = 800,
            recommendedLimit = 150000,
            businessName = "Test Company"
        )
        
        // Your test logic here...
    }
    
    @Test
    fun shouldHandleExperianTimeout() {
        // Add custom timeout scenario
        ExperianStubs.stubExperianTimeout(
            container = experianMockContainer,
            taxIdentifier = "12345678",
            delayMillis = 31000
        )
        
        // Your test logic here...
    }
}
```

**Note**: Custom stubs configured via `ExperianStubs` have higher priority than static mappings and will override them for the duration of the test. Call `ExperianStubs.resetStubs()` in `beforeEach()` to clear custom stubs between tests.

### Available Stub Methods

- `stubSuccessfulCreditScore()` - Stub successful credit check
- `stubDeclinedCreditScore()` - Stub low credit score
- `stubBusinessNotFound()` - Stub business not found
- `stubExperianTimeout()` - Stub timeout scenario
- `stubExperianError()` - Stub HTTP error (500, 503, etc.)
- `stubExperianServiceUnavailable()` - Stub 503 error
- `resetStubs()` - Clear all stubs

## Directory Structure

```
wiremock/
├── mappings/                          # WireMock stub mappings
│   ├── credit-score-success.json     # Default successful response
│   ├── credit-score-low.json         # Low credit score scenario
│   ├── credit-score-not-found.json   # Business not found
│   ├── credit-score-error.json       # 500 error scenario
│   ├── credit-score-timeout.json     # Timeout simulation
│   └── credit-score-service-unavailable.json  # 503 error
├── __files/                           # Response body files (if needed)
│   └── .gitkeep
└── README.md                          # This file
```

## WireMock Admin API

You can dynamically manage stubs using the WireMock admin API:

### View All Stubs

```bash
curl http://localhost:8089/__admin/mappings
```

### Reset All Stubs

```bash
curl -X POST http://localhost:8089/__admin/reset
```

### Add New Stub

```bash
curl -X POST http://localhost:8089/__admin/mappings \
  -H "Content-Type: application/json" \
  -d '{
    "request": {
      "method": "GET",
      "urlPath": "/api/v1/credit-score",
      "queryParameters": {
        "taxIdentifier": {
          "equalTo": "11111111"
        }
      }
    },
    "response": {
      "status": 200,
      "jsonBody": {
        "requestId": "test-123",
        "success": true,
        "result": { ... }
      }
    }
  }'
```

## Response Templating

WireMock supports response templating (enabled with `--global-response-templating`). Available helpers:

- `{{randomValue type='UUID'}}` - Generate random UUID
- `{{now}}` - Current timestamp
- `{{request.query.taxIdentifier}}` - Echo query parameter

Example in mapping:
```json
{
  "requestId": "{{randomValue type='UUID'}}",
  "timestamp": "{{now format='yyyy-MM-dd'T'HH:mm:ss'Z'}}"
}
```

## Troubleshooting

### Mock API Not Starting

Check if port 8089 is already in use:
```bash
lsof -i :8089
```

### Viewing Logs

```bash
docker-compose logs -f experian-mock
```

### Mapping Not Working

1. Check mapping file syntax is valid JSON
2. Verify priority values (lower number = higher priority)
3. Check WireMock logs for errors
4. Test mapping via admin API

## Resources

- [WireMock Documentation](https://wiremock.org/docs/)
- [Response Templating](https://wiremock.org/docs/response-templating/)
- [Request Matching](https://wiremock.org/docs/request-matching/)
