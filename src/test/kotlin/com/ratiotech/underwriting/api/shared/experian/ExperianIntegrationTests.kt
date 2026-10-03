package com.ratiotech.underwriting.api.shared.experian

import com.ratiotech.underwriting.api.clients.experian.ExperianClient
import com.ratiotech.underwriting.api.shared.IntegrationTestBase
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Integration tests demonstrating how to use the Experian mock API infrastructure.
 *
 * These tests show candidates how to:
 * 1. Use default static mappings loaded from wiremock/mappings/ directory
 * 2. Override defaults with custom stubs using ExperianStubs helper
 * 3. Test different scenarios (success, errors, timeouts)
 * 4. Verify ExperianClient behavior with mocked external dependencies
 *
 * NOTE: These tests verify the ExperianClient integration with WireMock.
 * Candidates should implement their own tests for the business logic that uses ExperianClient
 * (e.g., customer creation with credit score checks).
 */
class ExperianIntegrationTests : IntegrationTestBase() {

  @Autowired
  private lateinit var experianClient: ExperianClient

  override fun beforeEach() {
    // Reset WireMock stubs before each test to ensure clean state
    // This clears any custom stubs added programmatically but keeps static mappings
    ExperianStubs.resetStubs(experianMockContainer)
  }

  @Nested
  inner class DefaultStaticMappingTests {

    @Test
    fun `should use default mapping for any numeric tax identifier`() {
      // Given - No explicit stubbing needed, uses static mapping from wiremock/mappings/credit-score-success.json
      val taxIdentifier = "12345678"

      // When - Call ExperianClient
      val response = experianClient.getCreditScore(taxIdentifier)

      // Then - Verify default successful response (750 credit score, $100k limit)
      assertNotNull(response)
      assertEquals(true, response.success)
      assertNotNull(response.result)
      
      val result = response.result
      assertNotNull(result)
      val commercialScore = result.commercialScore
      assertNotNull(commercialScore)
      assertEquals(750L, commercialScore.score)
      assertEquals(100000L, commercialScore.recommendedCreditLimitAmount)
    }

    @Test
    fun `should use default mapping for low credit score scenario`() {
      // Given - Tax identifier 99999999 uses static mapping from wiremock/mappings/credit-score-low.json
      val taxIdentifier = "99999999"

      // When - Call ExperianClient
      val response = experianClient.getCreditScore(taxIdentifier)

      // Then - Verify low credit score response
      assertNotNull(response)
      assertEquals(true, response.success)
      assertNotNull(response.result)
      
      val result = response.result
      assertNotNull(result)
      val businessHeader = result.businessHeader
      assertNotNull(businessHeader)
      assertEquals("Risky Business LLC", businessHeader.businessName)
      
      val commercialScore = result.commercialScore
      assertNotNull(commercialScore)
      assertEquals(300L, commercialScore.score)
      assertEquals(0L, commercialScore.recommendedCreditLimitAmount)
    }

    @Test
    fun `should use default mapping for business not found scenario`() {
      // Given - Tax identifier 00000000 uses static mapping from wiremock/mappings/credit-score-not-found.json
      val taxIdentifier = "00000000"

      // When - Call ExperianClient
      val response = experianClient.getCreditScore(taxIdentifier)

      // Then - Verify business not found response
      assertNotNull(response)
      assertEquals(false, response.success)
      assertNull(response.result)
    }
  }

  @Nested
  inner class CustomStubTests {

    @Test
    fun `should override default mapping with custom high credit score`() {
      // Given - Override default mapping with custom stub
      val taxIdentifier = "12345678"
      ExperianStubs.stubSuccessfulCreditScore(
        container = experianMockContainer,
        taxIdentifier = taxIdentifier,
        creditScore = 800,
        recommendedLimit = 150000,
        businessName = "Test Company Inc."
      )

      // When - Call ExperianClient
      val response = experianClient.getCreditScore(taxIdentifier)

      // Then - Verify custom stub response (not default 750 score)
      assertNotNull(response)
      assertEquals(true, response.success)
      assertNotNull(response.result)
      
      val result = response.result
      assertNotNull(result)
      val businessHeader = result.businessHeader
      assertNotNull(businessHeader)
      assertEquals("Test Company Inc.", businessHeader.businessName)
      assertEquals(taxIdentifier, businessHeader.taxId)
      
      val commercialScore = result.commercialScore
      assertNotNull(commercialScore)
      assertEquals(800L, commercialScore.score)
      assertEquals(150000L, commercialScore.recommendedCreditLimitAmount)
    }

    @Test
    fun `should add custom stub for specific tax identifier`() {
      // Given - Add custom stub for specific scenario
      val taxIdentifier = "98765432"
      ExperianStubs.stubDeclinedCreditScore(
        container = experianMockContainer,
        taxIdentifier = taxIdentifier,
        creditScore = 350,
        businessName = "Custom Test Business"
      )

      // When - Call ExperianClient
      val response = experianClient.getCreditScore(taxIdentifier)

      // Then - Verify custom stub response
      assertNotNull(response)
      assertEquals(true, response.success)
      assertNotNull(response.result)
      
      val result = response.result
      assertNotNull(result)
      val businessHeader = result.businessHeader
      assertNotNull(businessHeader)
      assertEquals("Custom Test Business", businessHeader.businessName)
      
      val commercialScore = result.commercialScore
      assertNotNull(commercialScore)
      assertEquals(350L, commercialScore.score)
      assertEquals(0L, commercialScore.recommendedCreditLimitAmount)
    }

    @Test
    fun `should override default not found mapping with custom stub`() {
      // Given - Override the default 00000000 mapping
      val taxIdentifier = "44444444"
      ExperianStubs.stubBusinessNotFound(
        container = experianMockContainer,
        taxIdentifier = taxIdentifier
      )

      // When - Call ExperianClient
      val response = experianClient.getCreditScore(taxIdentifier)

      // Then - Verify business not found response
      assertNotNull(response)
      assertEquals(false, response.success)
      assertNull(response.result)
    }
  }

  @Nested
  inner class ErrorHandlingTests {

    @Test
    fun `should throw exception on Experian timeout`() {
      // Given - Stub Experian to timeout (exceeds 30 second client timeout)
      val taxIdentifier = "11111111"
      ExperianStubs.stubExperianTimeout(
        container = experianMockContainer,
        taxIdentifier = taxIdentifier,
        delayMillis = 31000
      )

      // When/Then - Verify RuntimeException is thrown
      val exception = assertThrows<RuntimeException> {
        experianClient.getCreditScore(taxIdentifier)
      }

      assertEquals(exception.message?.contains("Failed to retrieve credit score"), true)
      assertEquals(exception.message?.contains(taxIdentifier), true)
    }

    @Test
    fun `should throw exception on Experian service unavailable`() {
      // Given - Stub Experian to return 503 error
      val taxIdentifier = "22222222"
      ExperianStubs.stubExperianServiceUnavailable(
        container = experianMockContainer,
        taxIdentifier = taxIdentifier
      )

      // When/Then - Verify RuntimeException is thrown
      val exception = assertThrows<RuntimeException> {
        experianClient.getCreditScore(taxIdentifier)
      }

      assertEquals(exception.message?.contains("Failed to retrieve credit score"), true)
      assertEquals(exception.message?.contains(taxIdentifier), true)
    }

    @Test
    fun `should throw exception on Experian internal server error`() {
      // Given - Stub Experian to return 500 error
      val taxIdentifier = "33333333"
      ExperianStubs.stubExperianError(
        container = experianMockContainer,
        taxIdentifier = taxIdentifier,
        httpStatus = 500,
        errorMessage = "Internal server error from Experian"
      )

      // When/Then - Verify RuntimeException is thrown
      val exception = assertThrows<RuntimeException> {
        experianClient.getCreditScore(taxIdentifier)
      }

      assertEquals(exception.message?.contains("Failed to retrieve credit score"), true)
      assertEquals(exception.message?.contains(taxIdentifier), true)
    }
  }
}
