package com.ratiotech.underwriting.api.logic

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import com.ratiotech.underwriting.api.controllers.requests.CreateCustomerRequest
import com.ratiotech.underwriting.api.controllers.requests.UpdateCustomerModel
import com.ratiotech.underwriting.api.controllers.responses.Customer
import com.ratiotech.underwriting.api.entities.CustomerEntity
import com.ratiotech.underwriting.api.logic.translators.CustomerTranslator
import com.ratiotech.underwriting.api.repositories.CustomerRepository
import com.ratiotech.underwriting.api.shared.constants.Constants.SYSTEM_IDENTITY
import com.ratiotech.underwriting.api.shared.exceptions.BadRequestException
import com.ratiotech.underwriting.api.shared.exceptions.NotFoundException
import com.ratiotech.underwriting.api.shared.logic.JsonPatchManager
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.Date
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CustomerLogicTest {

  @MockK private lateinit var customerRepository: CustomerRepository

  @MockK private lateinit var customerTranslator: CustomerTranslator

  @MockK private lateinit var jsonPatchManager: JsonPatchManager

  @InjectMockKs private lateinit var classUnderTest: CustomerLogic

  @BeforeEach
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Nested
  inner class CreateCustomerTest {

    @Test
    fun `should create customer successfully`() {
      // Given
      val request = CreateCustomerRequest(name = "Acme Corporation", taxIdentifier = "12-3456789")

      val savedCustomerId = UUID.randomUUID()
      val savedCustomerEntity =
        CustomerEntity(
          name = "Acme Corporation",
          taxIdentifier = "12-3456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          id = savedCustomerId,
        )

      every { customerRepository.existsByTaxIdentifier("12-3456789") } returns false
      every { customerRepository.save(any<CustomerEntity>()) } returns savedCustomerEntity

      // When
      val result = classUnderTest.createCustomer(request)

      // Then
      assertNotNull(result)
      assertEquals(savedCustomerId, result)
      verify { customerRepository.existsByTaxIdentifier("12-3456789") }
      verify { customerRepository.save(any<CustomerEntity>()) }
    }

    @Test
    fun `should throw BadRequestException when tax identifier already exists`() {
      // Given
      val request = CreateCustomerRequest(name = "New Company", taxIdentifier = "EXISTING-TAX-123")

      every { customerRepository.existsByTaxIdentifier("EXISTING-TAX-123") } returns true

      // When & Then
      val exception =
        assertFailsWith<BadRequestException> { classUnderTest.createCustomer(request) }

      assertEquals(
        "Customer with tax identifier 'EXISTING-TAX-123' already exists",
        exception.message,
      )
      verify { customerRepository.existsByTaxIdentifier("EXISTING-TAX-123") }
      verify(exactly = 0) { customerRepository.save(any<CustomerEntity>()) }
    }
  }

  @Nested
  inner class GetAllCustomersTest {

    @Test
    fun `should get all customers successfully`() {
      // Given
      val customer1Entity =
        CustomerEntity(
          name = "Acme Corporation",
          taxIdentifier = "12-3456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          id = UUID.randomUUID(),
        )

      val customer2Entity =
        CustomerEntity(
          name = "Tech Solutions Ltd",
          taxIdentifier = "98-7654321",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          id = UUID.randomUUID(),
        )

      val customer1 =
        Customer(
          id = customer1Entity.id,
          name = "Acme Corporation",
          taxIdentifier = "12-3456789",
          createdDate = Date(),
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          lastModifiedDate = Date(),
        )

      val customer2 =
        Customer(
          id = customer2Entity.id,
          name = "Tech Solutions Ltd",
          taxIdentifier = "98-7654321",
          createdDate = Date(),
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          lastModifiedDate = Date(),
        )

      every { customerRepository.findAll() } returns listOf(customer1Entity, customer2Entity)
      every { customerTranslator.toModel(customer1Entity) } returns customer1
      every { customerTranslator.toModel(customer2Entity) } returns customer2

      // When
      val result = classUnderTest.getAllCustomers()

      // Then
      assertNotNull(result)
      assertEquals(2, result.size)
      assertTrue(result.contains(customer1))
      assertTrue(result.contains(customer2))
      verify { customerRepository.findAll() }
      verify { customerTranslator.toModel(customer1Entity) }
      verify { customerTranslator.toModel(customer2Entity) }
    }

    @Test
    fun `should get all customers when empty`() {
      // Given
      every { customerRepository.findAll() } returns emptyList()

      // When
      val result = classUnderTest.getAllCustomers()

      // Then
      assertNotNull(result)
      assertTrue(result.isEmpty())
      verify { customerRepository.findAll() }
      verify { customerTranslator wasNot Called }
    }

    @Test
    fun `should get all customers with single customer`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "Single Company",
          taxIdentifier = "SINGLE-123",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          id = UUID.randomUUID(),
        )

      val customer =
        Customer(
          id = customerEntity.id,
          name = "Single Company",
          taxIdentifier = "SINGLE-123",
          createdDate = Date(),
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          lastModifiedDate = Date(),
        )

      every { customerRepository.findAll() } returns listOf(customerEntity)
      every { customerTranslator.toModel(customerEntity) } returns customer

      // When
      val result = classUnderTest.getAllCustomers()

      // Then
      assertNotNull(result)
      assertEquals(1, result.size)
      assertEquals(customer, result[0])
      verify { customerRepository.findAll() }
      verify { customerTranslator.toModel(customerEntity) }
    }
  }

  @Nested
  inner class GetCustomerByIdTest {

    @Test
    fun `should get customer by id successfully`() {
      // Given
      val customerId = UUID.randomUUID()
      val customerEntity =
        CustomerEntity(
          name = "Acme Corporation",
          taxIdentifier = "12-3456789",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          id = customerId,
        )

      val expectedCustomer =
        Customer(
          id = customerId,
          name = "Acme Corporation",
          taxIdentifier = "12-3456789",
          createdDate = Date(),
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          lastModifiedDate = Date(),
        )

      every { customerRepository.findById(customerId) } returns Optional.of(customerEntity)
      every { customerTranslator.toModel(customerEntity) } returns expectedCustomer

      // When
      val result = classUnderTest.getCustomerById(customerId)

      // Then
      assertNotNull(result)
      assertEquals(expectedCustomer, result)
      verify { customerRepository.findById(customerId) }
      verify { customerTranslator.toModel(customerEntity) }
    }

    @Test
    fun `should get customer by id with minimal data`() {
      // Given
      val customerId = UUID.randomUUID()
      val customerEntity =
        CustomerEntity(
          name = "Basic Company",
          taxIdentifier = "BASIC-123",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = customerId,
        )

      val expectedCustomer =
        Customer(
          id = customerId,
          name = "Basic Company",
          taxIdentifier = "BASIC-123",
          createdDate = null,
          createdBy = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
        )

      every { customerRepository.findById(customerId) } returns Optional.of(customerEntity)
      every { customerTranslator.toModel(customerEntity) } returns expectedCustomer

      // When
      val result = classUnderTest.getCustomerById(customerId)

      // Then
      assertNotNull(result)
      assertEquals(expectedCustomer, result)
      verify { customerRepository.findById(customerId) }
      verify { customerTranslator.toModel(customerEntity) }
    }

    @Test
    fun `should throw NotFoundException when customer does not exist`() {
      // Given
      val customerId = UUID.randomUUID()
      every { customerRepository.findById(customerId) } returns Optional.empty()

      // When & Then
      val exception =
        assertFailsWith<NotFoundException> { classUnderTest.getCustomerById(customerId) }

      assertEquals("Customer not found with id: $customerId", exception.message)
      verify { customerRepository.findById(customerId) }
      verify { customerTranslator wasNot Called }
    }
  }

  @Nested
  inner class PatchCustomerTest {

    @Test
    fun `should patch customer successfully`() {
      // Given
      val customerId = UUID.randomUUID()
      val customerEntity =
        CustomerEntity(
          name = "Old Company Name",
          taxIdentifier = "OLD-TAX-123",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          id = customerId,
        )

      val updateModel =
        UpdateCustomerModel(name = "Old Company Name", taxIdentifier = "OLD-TAX-123")

      val patchedUpdateModel =
        UpdateCustomerModel(name = "New Company Name", taxIdentifier = "NEW-TAX-456")

      val updatedEntity =
        CustomerEntity(
          name = "New Company Name",
          taxIdentifier = "NEW-TAX-456",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          id = customerId,
        )

      val expectedCustomer =
        Customer(
          id = customerId,
          name = "New Company Name",
          taxIdentifier = "NEW-TAX-456",
          createdDate = Date(),
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          lastModifiedDate = Date(),
        )

      // Create a real JsonPatch object for testing
      val objectMapper = ObjectMapper()
      val patchJson = """[{"op": "replace", "path": "/name", "value": "New Company Name"}]"""
      val jsonPatch = JsonPatch.fromJson(objectMapper.readTree(patchJson))

      every { customerRepository.findById(customerId) } returns Optional.of(customerEntity)
      every { customerTranslator.toUpdateModel(customerEntity) } returns updateModel
      every {
        jsonPatchManager.applyPatch(jsonPatch, updateModel, UpdateCustomerModel::class.java)
      } returns patchedUpdateModel
      every { customerTranslator.updateEntity(customerEntity, patchedUpdateModel) } just runs
      every { customerRepository.save(customerEntity) } returns updatedEntity
      every { customerTranslator.toModel(updatedEntity) } returns expectedCustomer

      // When
      val result = classUnderTest.patchCustomer(customerId, jsonPatch)

      // Then
      assertNotNull(result)
      assertEquals(expectedCustomer, result)
      verify { customerRepository.findById(customerId) }
      verify { customerTranslator.toUpdateModel(customerEntity) }
      verify {
        jsonPatchManager.applyPatch(jsonPatch, updateModel, UpdateCustomerModel::class.java)
      }
      verify { customerTranslator.updateEntity(customerEntity, patchedUpdateModel) }
      verify { customerRepository.save(customerEntity) }
      verify { customerTranslator.toModel(updatedEntity) }
    }

    @Test
    fun `should throw NotFoundException when patching non-existent customer`() {
      // Given
      val customerId = UUID.randomUUID()

      // Create a simple mock JsonPatch to avoid complex object creation
      val jsonPatch = mockk<JsonPatch>()

      every { customerRepository.findById(customerId) } returns Optional.empty()

      // When & Then
      val exception =
        assertFailsWith<NotFoundException> { classUnderTest.patchCustomer(customerId, jsonPatch) }

      assertEquals("Customer not found with id: $customerId", exception.message)
    }

    @Test
    fun `should patch customer with partial update`() {
      // Given
      val customerId = UUID.randomUUID()
      val customerEntity =
        CustomerEntity(
          name = "Original Company",
          taxIdentifier = "ORIGINAL-TAX",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          id = customerId,
        )

      val updateModel =
        UpdateCustomerModel(name = "Original Company", taxIdentifier = "ORIGINAL-TAX")

      val patchedUpdateModel =
        UpdateCustomerModel(name = "Updated Company", taxIdentifier = "ORIGINAL-TAX")

      val updatedEntity =
        CustomerEntity(
          name = "Updated Company",
          taxIdentifier = "ORIGINAL-TAX",
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          id = customerId,
        )

      val expectedCustomer =
        Customer(
          id = customerId,
          name = "Updated Company",
          taxIdentifier = "ORIGINAL-TAX",
          createdDate = Date(),
          createdBy = SYSTEM_IDENTITY,
          lastModifiedBy = SYSTEM_IDENTITY,
          lastModifiedDate = Date(),
        )

      // Create a real JsonPatch object for testing
      val objectMapper = ObjectMapper()
      val patchJson = """[{"op": "replace", "path": "/name", "value": "Updated Company"}]"""
      val jsonPatch = JsonPatch.fromJson(objectMapper.readTree(patchJson))

      every { customerRepository.findById(customerId) } returns Optional.of(customerEntity)
      every { customerTranslator.toUpdateModel(customerEntity) } returns updateModel
      every {
        jsonPatchManager.applyPatch(jsonPatch, updateModel, UpdateCustomerModel::class.java)
      } returns patchedUpdateModel
      every { customerTranslator.updateEntity(customerEntity, patchedUpdateModel) } just runs
      every { customerRepository.save(customerEntity) } returns updatedEntity
      every { customerTranslator.toModel(updatedEntity) } returns expectedCustomer

      // When
      val result = classUnderTest.patchCustomer(customerId, jsonPatch)

      // Then
      assertNotNull(result)
      assertEquals(expectedCustomer, result)
      verify { customerRepository.findById(customerId) }
      verify { customerTranslator.toUpdateModel(customerEntity) }
      verify {
        jsonPatchManager.applyPatch(jsonPatch, updateModel, UpdateCustomerModel::class.java)
      }
      verify { customerTranslator.updateEntity(customerEntity, patchedUpdateModel) }
      verify { customerRepository.save(customerEntity) }
      verify { customerTranslator.toModel(updatedEntity) }
    }
  }

  @Nested
  inner class DeleteCustomerTest {

    @Test
    fun `should delete customer successfully`() {
      // Given
      val customerId = UUID.randomUUID()
      every { customerRepository.existsById(customerId) } returns true
      every { customerRepository.deleteById(customerId) } just runs

      // When
      classUnderTest.deleteCustomer(customerId)

      // Then
      verify { customerRepository.existsById(customerId) }
      verify { customerRepository.deleteById(customerId) }
    }

    @Test
    fun `should throw NotFoundException when deleting non-existent customer`() {
      // Given
      val customerId = UUID.randomUUID()
      every { customerRepository.existsById(customerId) } returns false

      // When & Then
      val exception =
        assertFailsWith<NotFoundException> { classUnderTest.deleteCustomer(customerId) }

      assertEquals("Customer not found with id: $customerId", exception.message)
      verify { customerRepository.existsById(customerId) }
      verify(inverse = true) { customerRepository.deleteById(any()) }
    }
  }
}
