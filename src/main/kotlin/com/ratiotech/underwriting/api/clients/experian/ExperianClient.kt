package com.ratiotech.underwriting.api.clients.experian

import com.fasterxml.jackson.databind.ObjectMapper
import com.ratiotech.underwriting.api.clients.experian.models.ExperianCommercialScoresResponse
import java.time.Duration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

/**
 * Client for communicating with the Experian Commercial Scores API.
 *
 * This client uses Spring WebClient to make HTTP requests to the Experian API
 * for retrieving commercial credit scores and business information.
 *
 * Configuration:
 * - Base URL is configured via the `EXPERIAN_BASE_URL` environment variable
 * - Default timeout is 30 seconds
 * - Responses are parsed using Jackson ObjectMapper
 *
 * @property experianBaseUrl The base URL for the Experian API
 * @property objectMapper Jackson ObjectMapper for JSON serialization/deserialization
 */
@Component
class ExperianClient(
  @Value("\${EXPERIAN_BASE_URL}") private val experianBaseUrl: String,
  private val objectMapper: ObjectMapper
) {

  /**
   * Retrieves commercial credit score information for a business.
   *
   * Makes a GET request to the Experian API to fetch credit score details
   * including business header information and commercial score.
   *
   * @param taxIdentifier The business tax identifier to look up
   * @return ExperianCommercialScoresResponse containing credit score and business details
   * @throws RuntimeException if the API call fails or times out
   */
  fun getCreditScore(taxIdentifier: String): ExperianCommercialScoresResponse {
    log.info("Fetching credit score from Experian for tax identifier: $taxIdentifier")
    
    return runCatching {
      getService().getCreditScore(taxIdentifier)
    }.onSuccess { response ->
      log.info(
        "Successfully retrieved credit score for tax identifier: $taxIdentifier, success: ${response.success}"
      )
    }.onFailure { e ->
      log.error("Error fetching credit score from Experian for tax identifier: $taxIdentifier", e)
    }.getOrElse { e ->
      throw RuntimeException(
        "Failed to retrieve credit score from Experian for tax identifier: $taxIdentifier",
        e
      )
    }
  }

  /**
   * Creates and configures the WebClient instance for Experian API calls.
   *
   * Configuration includes:
   * - Base URL from environment variable
   * - 30-second timeout
   * - JSON content type headers
   * - Custom Jackson codecs for JSON serialization
   *
   * @return Configured WebClient instance
   */
  private fun getWebClient(): WebClient {
    return WebClient.builder()
      .baseUrl(experianBaseUrl)
      .codecs { configurer ->
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(objectMapper))
        configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper))
        configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE)
      }
      .defaultHeaders { headers ->
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
      }
      .build()
  }

  /**
   * Creates the HTTP service proxy for the Experian API.
   *
   * Uses Spring's HttpServiceProxyFactory to create a client from the
   * ExperianService interface, with configured timeout and WebClient.
   *
   * @return ExperianService proxy instance
   */
  private fun getService(): ExperianService {
    val httpServiceProxyFactory =
      HttpServiceProxyFactory.builder(WebClientAdapter.forClient(getWebClient()))
        .blockTimeout(THIRTY_SECONDS)
        .build()

    return httpServiceProxyFactory.createClient(ExperianService::class.java)
  }

  companion object {
    private val log = LoggerFactory.getLogger(ExperianClient::class.java)
    private const val MAX_IN_MEMORY_SIZE = 256 * 1024 // 256 KB
    private val THIRTY_SECONDS: Duration = Duration.ofSeconds(30)
  }
}
