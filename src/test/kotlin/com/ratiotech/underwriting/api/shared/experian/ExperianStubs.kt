package com.ratiotech.underwriting.api.shared.experian

import com.github.tomakehurst.wiremock.client.WireMock

/**
 * Helper class for creating WireMock stubs for Experian API endpoints.
 *
 * This class provides convenient methods to stub various Experian API scenarios
 * for integration testing. All stubs are configured via the WireMock admin API.
 */
object ExperianStubs {

  /**
   * Stubs a successful credit score check response from Experian.
   *
   * @param container the WireMock container to configure
   * @param taxIdentifier the tax identifier to match in the request
   * @param creditScore the credit score to return (default 750)
   * @param recommendedLimit the recommended credit limit (default 100000)
   * @param businessName the business name to return in response
   */
  fun stubSuccessfulCreditScore(
    container: ExperianWireMockContainer,
    taxIdentifier: String,
    creditScore: Long = 750,
    recommendedLimit: Long = 100000,
    businessName: String = "Test Company Inc.",
  ) {
    val wireMock = createWireMockClient(container)

    wireMock.register(
      WireMock.get(WireMock.urlPathEqualTo("/api/v1/credit-score"))
        .withQueryParam("taxIdentifier", WireMock.equalTo(taxIdentifier))
        .willReturn(
          WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "requestId": "req-${System.currentTimeMillis()}",
                "success": true,
                "result": {
                  "businessHeader": {
                    "bin": "BIN${taxIdentifier.take(6)}",
                    "businessName": "$businessName",
                    "address": "123 Main St, Suite 100, New York, NY 10001",
                    "phone": "+1-555-0100",
                    "taxId": "$taxIdentifier",
                    "websiteUrl": "https://www.testcompany.com",
                    "legalBusinessName": "$businessName",
                    "dbaNames": ["Test Co", "TC Inc"]
                  },
                  "commercialScore": {
                    "score": $creditScore,
                    "recommendedCreditLimitAmount": $recommendedLimit
                  }
                }
              }
              """.trimIndent()
            )
        )
    )
  }

  /**
   * Stubs a declined credit score response (low credit score).
   *
   * @param container the WireMock container to configure
   * @param taxIdentifier the tax identifier to match in the request
   * @param creditScore the low credit score to return (default 300)
   * @param businessName the business name to return in response
   */
  fun stubDeclinedCreditScore(
    container: ExperianWireMockContainer,
    taxIdentifier: String,
    creditScore: Long = 300,
    businessName: String = "Risky Business LLC",
  ) {
    val wireMock = createWireMockClient(container)

    wireMock.register(
      WireMock.get(WireMock.urlPathEqualTo("/api/v1/credit-score"))
        .withQueryParam("taxIdentifier", WireMock.equalTo(taxIdentifier))
        .willReturn(
          WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "requestId": "req-${System.currentTimeMillis()}",
                "success": true,
                "result": {
                  "businessHeader": {
                    "bin": "BIN${taxIdentifier.take(6)}",
                    "businessName": "$businessName",
                    "address": "456 Risk Ave, Los Angeles, CA 90001",
                    "phone": "+1-555-0200",
                    "taxId": "$taxIdentifier",
                    "websiteUrl": "https://www.riskybusiness.com",
                    "legalBusinessName": "$businessName",
                    "dbaNames": []
                  },
                  "commercialScore": {
                    "score": $creditScore,
                    "recommendedCreditLimitAmount": 0
                  }
                }
              }
              """.trimIndent()
            )
        )
    )
  }

  /**
   * Stubs a business not found response from Experian.
   *
   * @param container the WireMock container to configure
   * @param taxIdentifier the tax identifier to match in the request
   */
  fun stubBusinessNotFound(
    container: ExperianWireMockContainer,
    taxIdentifier: String,
  ) {
    val wireMock = createWireMockClient(container)

    wireMock.register(
      WireMock.get(WireMock.urlPathEqualTo("/api/v1/credit-score"))
        .withQueryParam("taxIdentifier", WireMock.equalTo(taxIdentifier))
        .willReturn(
          WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              """
              {
                "requestId": "req-${System.currentTimeMillis()}",
                "success": false,
                "result": null
              }
              """.trimIndent()
            )
        )
    )
  }

  /**
   * Stubs an Experian API timeout scenario.
   *
   * This simulates the case where the Experian API takes too long to respond.
   * The delay is set to 31 seconds to exceed typical HTTP client timeouts.
   *
   * @param container the WireMock container to configure
   * @param taxIdentifier the tax identifier to match in the request (optional)
   * @param delayMillis the delay in milliseconds (default 31000 ms = 31 seconds)
   */
  fun stubExperianTimeout(
    container: ExperianWireMockContainer,
    taxIdentifier: String? = null,
    delayMillis: Int = 31000,
  ) {
    val wireMock = createWireMockClient(container)

    val requestBuilder = WireMock.get(WireMock.urlPathEqualTo("/api/v1/credit-score"))

    if (taxIdentifier != null) {
      requestBuilder.withQueryParam("taxIdentifier", WireMock.equalTo(taxIdentifier))
    }

    wireMock.register(
      requestBuilder.willReturn(
        WireMock.aResponse().withStatus(200).withFixedDelay(delayMillis)
      )
    )
  }

  /**
   * Stubs an Experian API error response.
   *
   * This simulates various HTTP error scenarios from the Experian API.
   *
   * @param container the WireMock container to configure
   * @param taxIdentifier the tax identifier to match in the request (optional)
   * @param httpStatus the HTTP status code to return (default 500)
   * @param errorMessage the error message to return
   */
  fun stubExperianError(
    container: ExperianWireMockContainer,
    taxIdentifier: String? = null,
    httpStatus: Int = 500,
    errorMessage: String = "Internal server error",
  ) {
    val wireMock = createWireMockClient(container)

    val requestBuilder = WireMock.get(WireMock.urlPathEqualTo("/api/v1/credit-score"))

    if (taxIdentifier != null) {
      requestBuilder.withQueryParam("taxIdentifier", WireMock.equalTo(taxIdentifier))
    }

    wireMock.register(
      requestBuilder.willReturn(
        WireMock.aResponse()
          .withStatus(httpStatus)
          .withHeader("Content-Type", "application/json")
          .withBody(
            """
            {
              "error": "$errorMessage",
              "status": $httpStatus
            }
            """.trimIndent()
          )
      )
    )
  }

  /**
   * Stubs a service unavailable (503) response from Experian.
   *
   * This simulates the case where the Experian service is temporarily down.
   *
   * @param container the WireMock container to configure
   * @param taxIdentifier the tax identifier to match in the request (optional)
   */
  fun stubExperianServiceUnavailable(
    container: ExperianWireMockContainer,
    taxIdentifier: String? = null,
  ) {
    stubExperianError(
      container = container,
      taxIdentifier = taxIdentifier,
      httpStatus = 503,
      errorMessage = "Service temporarily unavailable"
    )
  }

  /**
   * Resets all stubs in the WireMock server.
   *
   * This is useful for cleaning up stubs between tests.
   *
   * @param container the WireMock container to reset
   */
  fun resetStubs(container: ExperianWireMockContainer) {
    val wireMock = createWireMockClient(container)
    wireMock.resetMappings()
  }

  /**
   * Creates a WireMock client for the given container.
   *
   * @param container the WireMock container
   * @return a configured WireMock client
   */
  private fun createWireMockClient(container: ExperianWireMockContainer): WireMock {
    val host = container.host
    val port = container.getMappedPort(9090)
    return WireMock(host, port)
  }
}
