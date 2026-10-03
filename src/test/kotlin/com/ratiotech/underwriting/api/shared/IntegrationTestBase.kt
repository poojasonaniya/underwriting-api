package com.ratiotech.underwriting.api.shared

import com.ratiotech.underwriting.api.repositories.CustomerRepository
import com.ratiotech.underwriting.api.repositories.UserRepository
import com.ratiotech.underwriting.api.shared.experian.ExperianWireMockContainer
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Tag("integration-test")
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@Testcontainers
@SpringBootTest
abstract class IntegrationTestBase {

  @Autowired protected lateinit var customerRepository: CustomerRepository

  @Autowired protected lateinit var userRepository: UserRepository

  @Autowired protected lateinit var mockMvc: MockMvc

  @BeforeEach
  fun setup() {
    purgeDatabase()
    beforeEach()
  }

  @AfterEach
  fun tearDown() {
    unmockkAll()
  }

  protected abstract fun beforeEach()

  /** In order to be sure of each execution, we clean the database before each test. */
  protected fun purgeDatabase() {
    customerRepository.deleteAllInBatch()
    userRepository.deleteAllInBatch()
  }

  companion object {

    private val postgresqlContainer: PostgreSQLContainer<*> =
      PostgreSQLContainer("postgres:14.13")
        .withDatabaseName("boost_test")
        .withUsername("underwriting_user")
        .withPassword("test123")

    /**
     * WireMock container for mocking Experian API responses.
     * Exposed as protected to allow test classes to configure stubs.
     */
    @JvmStatic protected val experianMockContainer = ExperianWireMockContainer()

    init {
      postgresqlContainer.start()
      experianMockContainer.start()
    }

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      // Database properties
      registry.add("POSTGRES_URL") { postgresqlContainer.jdbcUrl }
      registry.add("POSTGRES_USERNAME") { postgresqlContainer.username }
      registry.add("POSTGRES_PASSWORD") { postgresqlContainer.password }

      // Experian mock API properties
      registry.add("EXPERIAN_BASE_URL") { experianMockContainer.getBaseUrl() }
    }
  }
}
