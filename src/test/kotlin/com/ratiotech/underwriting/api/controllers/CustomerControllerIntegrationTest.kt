package com.ratiotech.underwriting.api.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.ratiotech.underwriting.api.controllers.requests.CreateCustomerRequest
import com.ratiotech.underwriting.api.controllers.responses.Customer
import com.ratiotech.underwriting.api.controllers.responses.PagedResponse
import com.ratiotech.underwriting.api.entities.CustomerEntity
import com.ratiotech.underwriting.api.shared.IntegrationTestBase
import com.ratiotech.underwriting.api.shared.constants.Constants.SYSTEM_IDENTITY
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.json.JacksonTester
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Integration tests for CustomerController. Tests all endpoints including create, get, and delete
 * operations with real database interactions.
 */
class CustomerControllerIntegrationTest : IntegrationTestBase() {

  @Autowired private lateinit var objectMapper: ObjectMapper
  @Autowired private lateinit var customerJacksonTester: JacksonTester<Customer>
  @Autowired private lateinit var customerListJacksonTester: JacksonTester<List<Customer>>
  @Autowired private lateinit var problemDetailJacksonTester: JacksonTester<ProblemDetail>
  @Autowired
  private lateinit var pagedCustomerJacksonTester: JacksonTester<PagedResponse<Customer>>

  override fun beforeEach() {
    // Additional setup if needed
  }

  @Nested
  inner class CreateCustomerTest {

    @Test
    fun `should create customer successfully with valid request`() {
      // Given
      val request = CreateCustomerRequest(name = "John Doe", taxIdentifier = "123456789")

      // When & Then
      mockMvc
        .perform(
          post(CUSTOMERS_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated)
        .andExpect(header().exists("Location"))

      // Verify customer was persisted in database
      val customers = customerRepository.findAll()
      assertEquals(1, customers.size)
      val savedCustomer = customers[0]
      assertEquals("John Doe", savedCustomer.name)
      assertEquals("123456789", savedCustomer.taxIdentifier)
      assertNotNull(savedCustomer.id)
      assertNotNull(savedCustomer.createdDate)
    }

    @Test
    fun `should return bad request when tax identifier already exists`() {
      // Given - Create an existing customer
      val existingCustomer =
        CustomerEntity(
          name = "Existing Customer",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      customerRepository.save(existingCustomer)

      val request =
        CreateCustomerRequest(
          name = "John Doe",
          // Same tax identifier
          taxIdentifier = "123456789",
        )

      // When & Then
      val result = mockMvc
        .perform(
          post(CUSTOMERS_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest)
        .andReturn()

      val actual = problemDetailJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(HttpStatus.BAD_REQUEST.value(), actual.status)
      assertEquals("Customer with tax identifier '123456789' already exists", actual.detail)

      // Verify no additional customer was created
      val customers = customerRepository.findAll()
      // Only the existing customer should remain
      assertEquals(1, customers.size)
    }

    @Test
    fun `should return bad request when name is blank`() {
      // Given
      val request =
        CreateCustomerRequest(
          // Blank name
          name = "",
          taxIdentifier = "123456789",
        )

      // When & Then
      val result =
        mockMvc
          .perform(
            post(CUSTOMERS_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isBadRequest)
          .andReturn()

      val problem = problemDetailJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.status)
      assertTrue(problem.detail!!.contains("'name' is required"))

      // Verify no customer was created
      val customers = customerRepository.findAll()
      assertTrue(customers.isEmpty())
    }

    @Test
    fun `should return bad request when tax identifier is blank`() {
      // Given
      val request =
        CreateCustomerRequest(
          name = "John Doe",
          // Blank tax identifier
          taxIdentifier = "",
        )

      // When & Then
      val result =
        mockMvc
          .perform(
            post(CUSTOMERS_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isBadRequest)
          .andReturn()

      val problem = problemDetailJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.status)
      assertTrue(problem.detail!!.contains("'taxIdentifier' is required"))

      // Verify no customer was created
      val customers = customerRepository.findAll()
      assertTrue(customers.isEmpty())
    }

    @Test
    fun `should return bad request when request body is malformed JSON`() {
      // Given
      // Missing value
      val malformedJson = """{"name": "John Doe", "taxIdentifier":}"""

      // When & Then
      mockMvc
        .perform(
          post(CUSTOMERS_PATH).contentType(MediaType.APPLICATION_JSON).content(malformedJson)
        )
        .andExpect(status().isBadRequest)

      // Verify no customer was created
      val customers = customerRepository.findAll()
      assertTrue(customers.isEmpty())
    }

    @Test
    fun `should return bad request when content type is not JSON`() {
      // Given
      val request = CreateCustomerRequest(name = "John Doe", taxIdentifier = "123456789")

      // When & Then
      mockMvc
        .perform(
          post(CUSTOMERS_PATH)
            // Wrong content type
            .contentType(MediaType.TEXT_PLAIN)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isUnsupportedMediaType)

      // Verify no customer was created
      val customers = customerRepository.findAll()
      assertTrue(customers.isEmpty())
    }

    @Test
    fun `should create multiple customers with different tax identifiers`() {
      // Given
      val request1 = CreateCustomerRequest(name = "John Doe", taxIdentifier = "123456789")
      val request2 = CreateCustomerRequest(name = "Jane Smith", taxIdentifier = "987654321")

      // When - Create first customer
      mockMvc
        .perform(
          post(CUSTOMERS_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request1))
        )
        .andExpect(status().isCreated)

      // When - Create second customer
      mockMvc
        .perform(
          post(CUSTOMERS_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request2))
        )
        .andExpect(status().isCreated)

      // Then - Verify both customers were created
      val customers = customerRepository.findAll()
      assertEquals(2, customers.size)

      val customerNames = customers.map { it.name }.sortedBy { it }
      val taxIdentifiers = customers.map { it.taxIdentifier }.sortedBy { it }

      assertEquals(listOf("Jane Smith", "John Doe"), customerNames)
      assertEquals(listOf("123456789", "987654321"), taxIdentifiers)
    }

    @Test
    fun `should return location header with correct customer ID`() {
      // Given
      val request = CreateCustomerRequest(name = "John Doe", taxIdentifier = "123456789")

      // When & Then
      val result =
        mockMvc
          .perform(
            post(CUSTOMERS_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request))
          )
          .andExpect(status().isCreated)
          .andExpect(header().exists("Location"))
          .andReturn()

      // Verify location header contains valid UUID
      val locationHeader = result.response.getHeader("Location")
      assertNotNull(locationHeader)
      assertTrue(locationHeader.contains("/customers/"))

      val customerId = locationHeader.substringAfterLast("/")
      // Should not throw exception if valid UUID
      assertNotNull(UUID.fromString(customerId))

      // Verify the customer exists with that ID
      val savedCustomer = customerRepository.findById(UUID.fromString(customerId))
      assertTrue(savedCustomer.isPresent)
      assertEquals("John Doe", savedCustomer.get().name)
      assertEquals("123456789", savedCustomer.get().taxIdentifier)
    }
  }

  @Nested
  inner class GetAllCustomersTest {

    @Test
    fun `should return empty list when no customers exist`() {
      // When & Then
      val result = mockMvc.perform(get(CUSTOMERS_PATH)).andExpect(status().isOk).andReturn()
      val customers = customerListJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(0, customers.size)
    }

    @Test
    fun `should return single customer when one exists`() {
      // Given
      val customer =
        CustomerEntity(
          name = "John Doe",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      // When & Then
      val result = mockMvc.perform(get(CUSTOMERS_PATH)).andExpect(status().isOk).andReturn()
      val customers = customerListJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(1, customers.size)
      val c = customers.first()
      assertEquals(savedCustomer.id, c.id)
      assertEquals("John Doe", c.name)
      assertEquals("123456789", c.taxIdentifier)
      assertNotNull(c.createdDate)
      assertEquals(SYSTEM_IDENTITY, c.createdBy)
      assertEquals(SYSTEM_IDENTITY, c.lastModifiedBy)
      assertNotNull(c.lastModifiedDate)
    }

    @Test
    fun `should return multiple customers when several exist`() {
      // Given
      val customer1 =
        CustomerEntity(
          name = "John Doe",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val customer2 =
        CustomerEntity(
          name = "Jane Smith",
          taxIdentifier = "987654321",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val customer3 =
        CustomerEntity(
          name = "Bob Johnson",
          taxIdentifier = "555666777",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )

      val savedCustomers = customerRepository.saveAll(listOf(customer1, customer2, customer3))

      // When & Then
      val result = mockMvc.perform(get(CUSTOMERS_PATH)).andExpect(status().isOk).andReturn()
      val customers = customerListJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(3, customers.size)

      val ids = customers.map { it.id?.toString() }.toSet()
      val expectedIds = savedCustomers.map { it.id.toString() }.toSet()
      assertEquals(expectedIds, ids)

      val names = customers.map { it.name }.toSet()
      assertEquals(setOf("John Doe", "Jane Smith", "Bob Johnson"), names)

      val tins = customers.map { it.taxIdentifier }.toSet()
      assertEquals(setOf("123456789", "987654321", "555666777"), tins)
    }

    @Test
    fun `should return customers with all required fields populated`() {
      // Given
      val customer =
        CustomerEntity(
          name = "Test Customer",
          taxIdentifier = "999888777",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      customerRepository.save(customer)

      // When & Then
      val result = mockMvc.perform(get(CUSTOMERS_PATH)).andExpect(status().isOk).andReturn()
      val customers = customerListJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(1, customers.size)
      val c = customers.first()
      assertNotNull(c.id)
      assertEquals("Test Customer", c.name)
      assertEquals("999888777", c.taxIdentifier)
      assertNotNull(c.createdDate)
      assertEquals(SYSTEM_IDENTITY, c.createdBy)
      assertEquals(SYSTEM_IDENTITY, c.lastModifiedBy)
      assertNotNull(c.lastModifiedDate)
    }
  }

  @Nested
  inner class GetCustomersPageTest {

    @Test
    fun `should return first page with default size`() {
      // Given
      createCustomers(25)

      // When
      val response = getPage(sort = "name,asc")

      // Then
      assertEquals(20, response.content.size)
      assertEquals("Customer 01", response.content.first().name)
      assertEquals("Customer 20", response.content.last().name)
      assertEquals(0, response.page.number)
      assertEquals(20, response.page.size)
      assertEquals(25, response.page.totalElements)
      assertEquals(2, response.page.totalPages)
    }

    @Test
    fun `should return middle page`() {
      // Given
      createCustomers(25)

      // When
      val response = getPage(page = "1", size = "10", sort = "name,asc")

      // Then
      assertEquals(10, response.content.size)
      assertEquals("Customer 11", response.content.first().name)
      assertEquals("Customer 20", response.content.last().name)
      assertEquals(1, response.page.number)
      assertEquals(10, response.page.size)
      assertEquals(25, response.page.totalElements)
      assertEquals(3, response.page.totalPages)
    }

    @Test
    fun `should return last page with remaining items`() {
      // Given
      createCustomers(25)

      // When
      val response = getPage(page = "2", size = "10", sort = "name,asc")

      // Then
      assertEquals(
        listOf("Customer 21", "Customer 22", "Customer 23", "Customer 24", "Customer 25"),
        response.content.map { it.name },
      )
      assertEquals(2, response.page.number)
      assertEquals(25, response.page.totalElements)
      assertEquals(3, response.page.totalPages)
    }

    @Test
    fun `should respect size parameter`() {
      // Given
      createCustomers(10)

      // When
      val response = getPage(size = "3")

      // Then
      assertEquals(3, response.content.size)
      assertEquals(3, response.page.size)
      assertEquals(10, response.page.totalElements)
      assertEquals(4, response.page.totalPages)
    }

    @Test
    fun `should not return the same customer on two pages`() {
      // Given
      val saved = createCustomers(7)

      // When
      val ids = (0..2).flatMap { page -> getPage(page = "$page", size = "3").content.map { it.id } }

      // Then
      assertEquals(7, ids.size)
      assertEquals(saved.map { it.id }.toSet(), ids.toSet())
    }

    @Test
    fun `should return customers with all fields populated`() {
      // Given
      val saved = createCustomers(1).first()

      // When
      val customer = getPage().content.single()

      // Then
      assertEquals(saved.id, customer.id)
      assertEquals("Customer 01", customer.name)
      assertEquals("TAX01", customer.taxIdentifier)
      assertNotNull(customer.createdDate)
      assertEquals(SYSTEM_IDENTITY, customer.createdBy)
      assertEquals(SYSTEM_IDENTITY, customer.lastModifiedBy)
      assertNotNull(customer.lastModifiedDate)
    }

    @Test
    fun `should sort by name ascending`() {
      // Given
      createCustomers(5)

      // When
      val response = getPage(sort = "name,asc")

      // Then
      assertEquals(
        listOf("Customer 01", "Customer 02", "Customer 03", "Customer 04", "Customer 05"),
        response.content.map { it.name },
      )
    }

    @Test
    fun `should sort by name descending`() {
      // Given
      createCustomers(5)

      // When
      val response = getPage(sort = "name,desc")

      // Then
      assertEquals(
        listOf("Customer 05", "Customer 04", "Customer 03", "Customer 02", "Customer 01"),
        response.content.map { it.name },
      )
    }

    @Test
    fun `should sort ascending when direction is omitted`() {
      // Given
      createCustomers(3)

      // When
      val response = getPage(sort = "name")

      // Then
      assertEquals(
        listOf("Customer 01", "Customer 02", "Customer 03"),
        response.content.map { it.name },
      )
    }

    @Test
    fun `should accept upper case sort direction`() {
      // Given
      createCustomers(3)

      // When
      val response = getPage(sort = "name,DESC")

      // Then
      assertEquals(
        listOf("Customer 03", "Customer 02", "Customer 01"),
        response.content.map { it.name },
      )
    }

    @Test
    fun `should sort by tax identifier descending`() {
      // Given
      createCustomers(3)

      // When
      val response = getPage(sort = "taxIdentifier,desc")

      // Then
      assertEquals(listOf("TAX03", "TAX02", "TAX01"), response.content.map { it.taxIdentifier })
    }

    @Test
    fun `should return empty content when no customers exist`() {
      // When
      val response = getPage()

      // Then
      assertTrue(response.content.isEmpty())
      assertEquals(0, response.page.number)
      assertEquals(20, response.page.size)
      assertEquals(0, response.page.totalElements)
      assertEquals(0, response.page.totalPages)
    }

    @Test
    fun `should return empty content when page is beyond last page`() {
      // Given
      createCustomers(5)

      // When
      val response = getPage(page = "5", size = "10")

      // Then
      assertTrue(response.content.isEmpty())
      assertEquals(5, response.page.number)
      assertEquals(5, response.page.totalElements)
      assertEquals(1, response.page.totalPages)
    }

    @Test
    fun `should return 400 when page is negative`() {
      mockMvc.perform(get(CUSTOMERS_PAGED_PATH).param("page", "-1")).andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when size is zero`() {
      mockMvc.perform(get(CUSTOMERS_PAGED_PATH).param("size", "0")).andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when size is greater than 100`() {
      mockMvc
        .perform(get(CUSTOMERS_PAGED_PATH).param("size", "101"))
        .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when page is not a number`() {
      mockMvc.perform(get(CUSTOMERS_PAGED_PATH).param("page", "abc")).andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when sort field is not allowed`() {
      // When
      val problem = getBadRequest(sort = "password,asc")

      // Then
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.status)
      assertEquals(
        "Invalid sort field 'password'. Allowed fields: name, taxIdentifier, createdDate, lastModifiedDate",
        problem.detail,
      )
    }

    @Test
    fun `should return 400 when sort direction is invalid`() {
      // When
      val problem = getBadRequest(sort = "name,sideways")

      // Then
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.status)
      assertEquals("Invalid sort direction 'sideways'. Allowed values: asc, desc", problem.detail)
    }

    @Test
    fun `should return 400 when sort has too many parts`() {
      // When
      val problem = getBadRequest(sort = "name,asc,extra")

      // Then
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.status)
      assertEquals(
        "Invalid sort 'name,asc,extra'. Expected format: field,direction",
        problem.detail,
      )
    }

    /** Saves [count] customers named "Customer 01", "Customer 02", ... so text order matches number order. */
    private fun createCustomers(count: Int): List<CustomerEntity> =
      customerRepository.saveAll(
        (1..count).map { i ->
          CustomerEntity(
            name = "Customer %02d".format(i),
            taxIdentifier = "TAX%02d".format(i),
            createdBy = SYSTEM_IDENTITY,
            lastModifiedBy = SYSTEM_IDENTITY,
          )
        }
      )

    /** Calls the paged endpoint, expects 200 OK and parses the body. Null params are not sent. */
    private fun getPage(
      page: String? = null,
      size: String? = null,
      sort: String? = null,
    ): PagedResponse<Customer> {
      val result =
        mockMvc.perform(pagedRequest(page, size, sort)).andExpect(status().isOk).andReturn()
      return pagedCustomerJacksonTester.parseObject(result.response.contentAsString)
    }

    /** Calls the paged endpoint, expects 400 Bad Request and parses the problem detail. */
    private fun getBadRequest(sort: String): ProblemDetail {
      val result =
        mockMvc
          .perform(pagedRequest(sort = sort))
          .andExpect(status().isBadRequest)
          .andReturn()
      return problemDetailJacksonTester.parseObject(result.response.contentAsString)
    }

    private fun pagedRequest(page: String? = null, size: String? = null, sort: String? = null) =
      get(CUSTOMERS_PAGED_PATH).apply {
        page?.let { param("page", it) }
        size?.let { param("size", it) }
        sort?.let { param("sort", it) }
      }
  }

  @Nested
  inner class GetCustomerByIdTest {

    @Test
    fun `should return customer when valid ID is provided`() {
      // Given
      val customer =
        CustomerEntity(
          name = "John Doe",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      // When & Then
      val result =
        mockMvc
          .perform(get(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString())))
          .andExpect(status().isOk)
          .andReturn()
      val c = customerJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(savedCustomer.id, c.id)
      assertEquals("John Doe", c.name)
      assertEquals("123456789", c.taxIdentifier)
      assertNotNull(c.createdDate)
      assertEquals(SYSTEM_IDENTITY, c.createdBy)
      assertEquals(SYSTEM_IDENTITY, c.lastModifiedBy)
      assertNotNull(c.lastModifiedDate)
    }

    @Test
    fun `should return 404 when customer ID does not exist`() {
      // Given
      val nonExistentId = UUID.randomUUID()

      // When & Then
      val result =
        mockMvc
          .perform(get(CUSTOMERS_ID_PATH.format(nonExistentId.toString())))
          .andExpect(status().isNotFound)
          .andReturn()
      val problem = problemDetailJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
      assertEquals("Customer not found with id: $nonExistentId", problem.detail)
    }

    @Test
    fun `should return customer with all audit fields populated`() {
      // Given
      val customer =
        CustomerEntity(
          name = "Test Customer",
          taxIdentifier = "999888777",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      // When & Then
      val result =
        mockMvc
          .perform(get(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString())))
          .andExpect(status().isOk)
          .andReturn()
      val c = customerJacksonTester.parseObject(result.response.contentAsString)
      assertNotNull(c.id)
      assertEquals("Test Customer", c.name)
      assertEquals("999888777", c.taxIdentifier)
      assertNotNull(c.createdDate)
      assertEquals(SYSTEM_IDENTITY, c.createdBy)
      assertEquals(SYSTEM_IDENTITY, c.lastModifiedBy)
      assertNotNull(c.lastModifiedDate)
    }

    @Test
    fun `should return different customers for different IDs`() {
      // Given
      val customer1 =
        CustomerEntity(
          name = "John Doe",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val customer2 =
        CustomerEntity(
          name = "Jane Smith",
          taxIdentifier = "987654321",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )

      val savedCustomer1 = customerRepository.save(customer1)
      val savedCustomer2 = customerRepository.save(customer2)

      // When & Then - Get first customer
      var result =
        mockMvc
          .perform(get(CUSTOMERS_ID_PATH.format(savedCustomer1.id.toString())))
          .andExpect(status().isOk)
          .andReturn()
      var c = customerJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(savedCustomer1.id, c.id)
      assertEquals("John Doe", c.name)
      assertEquals("123456789", c.taxIdentifier)

      // When & Then - Get second customer
      result =
        mockMvc
          .perform(get(CUSTOMERS_ID_PATH.format(savedCustomer2.id.toString())))
          .andExpect(status().isOk)
          .andReturn()
      c = customerJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(savedCustomer2.id, c.id)
      assertEquals("Jane Smith", c.name)
      assertEquals("987654321", c.taxIdentifier)
    }

    @Test
    fun `should return customer after modification`() {
      // Given - Create and modify a customer
      val customer =
        CustomerEntity(
          name = "Original Name",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      // Modify the customer
      savedCustomer.name = "Modified Name"
      val updatedCustomer = customerRepository.save(savedCustomer)

      // When & Then
      val result =
        mockMvc
          .perform(get(CUSTOMERS_ID_PATH.format(updatedCustomer.id.toString())))
          .andExpect(status().isOk)
          .andReturn()
      val c = customerJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(updatedCustomer.id, c.id)
      assertEquals("Modified Name", c.name)
      assertEquals("123456789", c.taxIdentifier)
      assertEquals(SYSTEM_IDENTITY, c.createdBy)
      assertNotNull(c.lastModifiedBy)
      assertNotNull(c.lastModifiedDate)
    }

    @Test
    fun `should return 404 for deleted customer`() {
      // Given
      val customer =
        CustomerEntity(
          name = "To Be Deleted",
          taxIdentifier = "999999999",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)
      val customerId = savedCustomer.id

      // Delete the customer
      customerRepository.delete(savedCustomer)

      // When & Then
      val result =
        mockMvc
          .perform(get(CUSTOMERS_ID_PATH.format(customerId)))
          .andExpect(status().isNotFound)
          .andReturn()
      val problem = problemDetailJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
      assertEquals("Customer not found with id: $customerId", problem.detail)
    }
  }

  @Nested
  inner class PatchCustomerTest {

    @Test
    fun `should update customer name successfully`() {
      // Given
      val customer =
        CustomerEntity(
          name = "Original Name",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      val patchJson =
        """
        [
          {
            "op": "replace",
            "path": "/name",
            "value": "Updated Name"
          }
        ]
      """
          .trimIndent()

      // When & Then
      val result =
        mockMvc
          .perform(
            patch(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString()))
              .contentType("application/json-patch+json")
              .content(patchJson)
          )
          .andExpect(status().isOk)
          .andReturn()
      val c = customerJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(savedCustomer.id, c.id)
      assertEquals("Updated Name", c.name)
      assertEquals("123456789", c.taxIdentifier)
      assertNotNull(c.createdDate)
      assertEquals(SYSTEM_IDENTITY, c.createdBy)
      assertNotNull(c.lastModifiedBy)
      assertNotNull(c.lastModifiedDate)

      // Verify database was updated
      val updatedCustomer = customerRepository.findById(savedCustomer.id!!).get()
      assertEquals("Updated Name", updatedCustomer.name)
      assertEquals("123456789", updatedCustomer.taxIdentifier)
    }

    @Test
    fun `should update customer tax identifier successfully`() {
      // Given
      val customer =
        CustomerEntity(
          name = "John Doe",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      val patchJson =
        """
        [
          {
            "op": "replace",
            "path": "/taxIdentifier",
            "value": "987654321"
          }
        ]
      """
          .trimIndent()

      // When & Then
      val result =
        mockMvc
          .perform(
            patch(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString()))
              .contentType("application/json-patch+json")
              .content(patchJson)
          )
          .andExpect(status().isOk)
          .andReturn()
      val c = customerJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(savedCustomer.id, c.id)
      assertEquals("John Doe", c.name)
      assertEquals("987654321", c.taxIdentifier)
      assertNotNull(c.createdDate)
      assertEquals(SYSTEM_IDENTITY, c.createdBy)
      assertNotNull(c.lastModifiedBy)
      assertNotNull(c.lastModifiedDate)

      // Verify database was updated
      val updatedCustomer = customerRepository.findById(savedCustomer.id!!).get()
      assertEquals("John Doe", updatedCustomer.name)
      assertEquals("987654321", updatedCustomer.taxIdentifier)
    }

    @Test
    fun `should update multiple fields simultaneously`() {
      // Given
      val customer =
        CustomerEntity(
          name = "Original Name",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      val patchJson =
        """
        [
          {
            "op": "replace",
            "path": "/name",
            "value": "Updated Name"
          },
          {
            "op": "replace",
            "path": "/taxIdentifier",
            "value": "999888777"
          }
        ]
      """
          .trimIndent()

      // When & Then
      val result =
        mockMvc
          .perform(
            patch(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString()))
              .contentType("application/json-patch+json")
              .content(patchJson)
          )
          .andExpect(status().isOk)
          .andReturn()
      val c = customerJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(savedCustomer.id, c.id)
      assertEquals("Updated Name", c.name)
      assertEquals("999888777", c.taxIdentifier)
      assertNotNull(c.createdDate)
      assertEquals(SYSTEM_IDENTITY, c.createdBy)
      assertNotNull(c.lastModifiedBy)
      assertNotNull(c.lastModifiedDate)

      // Verify database was updated
      val updatedCustomer = customerRepository.findById(savedCustomer.id!!).get()
      assertEquals("Updated Name", updatedCustomer.name)
      assertEquals("999888777", updatedCustomer.taxIdentifier)
    }

    @Test
    fun `should return 404 when customer ID does not exist`() {
      // Given
      val nonExistentId = UUID.randomUUID()
      val patchJson =
        """
        [
          {
            "op": "replace",
            "path": "/name",
            "value": "Updated Name"
          }
        ]
      """
          .trimIndent()

      // When & Then
      val result =
        mockMvc
          .perform(
            patch(CUSTOMERS_ID_PATH.format(nonExistentId.toString()))
              .contentType("application/json-patch+json")
              .content(patchJson)
          )
          .andExpect(status().isNotFound)
          .andReturn()
      val problem = problemDetailJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
      assertEquals("Customer not found with id: $nonExistentId", problem.detail)
    }

    @Test
    fun `should return 400 when patch JSON is malformed`() {
      // Given
      val customer =
        CustomerEntity(
          name = "John Doe",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      val malformedPatchJson =
        """
        [
          {
            "op": "replace",
            "path": "/name",
            "value": "Valid Name"
          },
          {
            "op": "invalid"
          }
        ]
      """
          .trimIndent()

      // When & Then
      mockMvc
        .perform(
          patch(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString()))
            .contentType("application/json-patch+json")
            .content(malformedPatchJson)
        )
        .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when patch operation is invalid`() {
      // Given
      val customer =
        CustomerEntity(
          name = "John Doe",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      val invalidPatchJson =
        """
        [
          {
            "op": "invalid-operation",
            "path": "/name",
            "value": "Updated Name"
          }
        ]
      """
          .trimIndent()

      // When & Then
      mockMvc
        .perform(
          patch(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString()))
            .contentType("application/json-patch+json")
            .content(invalidPatchJson)
        )
        .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when trying to patch invalid field`() {
      // Given
      val customer =
        CustomerEntity(
          name = "John Doe",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      val patchJson =
        """
        [
          {
            "op": "replace",
            "path": "/invalidField",
            "value": "Some Value"
          }
        ]
      """
          .trimIndent()

      // When & Then
      mockMvc
        .perform(
          patch(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString()))
            .contentType("application/json-patch+json")
            .content(patchJson)
        )
        .andExpect(status().isBadRequest)
    }
  }

  @Nested
  inner class DeleteCustomerTest {

    @Test
    fun `should successfully delete existing customer`() {
      // Given
      val customer =
        CustomerEntity(
          name = "John Doe",
          taxIdentifier = "123456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      // When & Then
      mockMvc
        .perform(delete(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString())))
        .andExpect(status().isNoContent)

      // Verify customer is deleted from database
      val deletedCustomer = customerRepository.findById(savedCustomer.id!!)
      assertTrue(deletedCustomer.isEmpty)
    }

    @Test
    fun `should return 404 when trying to delete non-existent customer`() {
      // Given
      val nonExistentId = UUID.randomUUID()

      // When & Then
      val result =
        mockMvc
          .perform(delete(CUSTOMERS_ID_PATH.format(nonExistentId.toString())))
          .andExpect(status().isNotFound)
          .andReturn()
      val problem = problemDetailJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
      assertEquals("Customer not found with id: $nonExistentId", problem.detail)
    }

    @Test
    fun `should return 404 when trying to delete already deleted customer`() {
      // Given
      val customer =
        CustomerEntity(
          name = "Jane Smith",
          taxIdentifier = "987654321",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
        )
      val savedCustomer = customerRepository.save(customer)

      // First deletion
      mockMvc
        .perform(delete(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString())))
        .andExpect(status().isNoContent)

      // When & Then - Try to delete again
      val result =
        mockMvc
          .perform(delete(CUSTOMERS_ID_PATH.format(savedCustomer.id.toString())))
          .andExpect(status().isNotFound)
          .andReturn()
      val problem = problemDetailJacksonTester.parseObject(result.response.contentAsString)
      assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
      assertEquals("Customer not found with id: ${savedCustomer.id}", problem.detail)
    }

    @Test
    fun `should return 400 when customer ID is invalid UUID format`() {
      // Given
      val invalidId = "invalid-uuid-format"

      // When & Then
      mockMvc.perform(delete(CUSTOMERS_ID_PATH.format(invalidId))).andExpect(status().isBadRequest)
    }
  }

  companion object {
    private const val CUSTOMERS_PATH = "/v1/customers"
    private const val CUSTOMERS_ID_PATH = "/v1/customers/%s"
    private const val CUSTOMERS_PAGED_PATH = "/v1/customers/paged"
  }
}
