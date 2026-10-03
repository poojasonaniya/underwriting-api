package com.ratiotech.underwriting.api.logic.translators

import com.ratiotech.underwriting.api.controllers.requests.UpdateCustomerModel
import com.ratiotech.underwriting.api.entities.CustomerEntity
import java.util.Date
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** Unit tests for CustomerTranslator. Tests all translation methods with various data scenarios. */
class CustomerTranslatorTest {

  private lateinit var classUnderTest: CustomerTranslator

  @BeforeEach
  fun setUp() {
    classUnderTest = CustomerTranslator()
  }

  @Nested
  inner class ToModelTest {

    @Test
    fun `should convert CustomerEntity to Customer with all fields`() {
      // Given
      val customerId = UUID.randomUUID()
      val createdBy = UUID.randomUUID()
      val lastModifiedBy = UUID.randomUUID()
      val createdDate = Date()
      val lastModifiedDate = Date()

      val customerEntity =
        CustomerEntity(
          name = "Acme Corporation",
          taxIdentifier = "12-3456789",
          createdBy = createdBy,
          createdDate = createdDate,
          lastModifiedBy = lastModifiedBy,
          lastModifiedDate = lastModifiedDate,
          id = customerId,
        )

      // When
      val result = classUnderTest.toModel(customerEntity)

      // Then
      assertNotNull(result)
      assertEquals(customerId, result.id)
      assertEquals("Acme Corporation", result.name)
      assertEquals("12-3456789", result.taxIdentifier)
      assertEquals(createdDate, result.createdDate)
      assertEquals(createdBy, result.createdBy)
      assertEquals(lastModifiedBy, result.lastModifiedBy)
      assertEquals(lastModifiedDate, result.lastModifiedDate)
    }

    @Test
    fun `should convert CustomerEntity to Customer with required fields only`() {
      // Given
      val customerId = UUID.randomUUID()
      val customerEntity =
        CustomerEntity(
          name = "Tech Solutions Ltd",
          taxIdentifier = "98-7654321",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = customerId,
        )

      // When
      val result = classUnderTest.toModel(customerEntity)

      // Then
      assertNotNull(result)
      assertEquals(customerId, result.id)
      assertEquals("Tech Solutions Ltd", result.name)
      assertEquals("98-7654321", result.taxIdentifier)
      assertNull(result.createdDate)
      assertNull(result.createdBy)
      assertNull(result.lastModifiedBy)
      assertNull(result.lastModifiedDate)
    }

    @Test
    fun `should convert CustomerEntity with special characters`() {
      // Given
      val customerId = UUID.randomUUID()
      val createdBy = UUID.randomUUID()
      val lastModifiedBy = UUID.randomUUID()
      val createdDate = Date()
      val lastModifiedDate = Date()

      val customerEntity =
        CustomerEntity(
          name = "José María & O'Connor-Smith Ltd.",
          taxIdentifier = "ES-B12345678",
          createdBy = createdBy,
          createdDate = createdDate,
          lastModifiedBy = lastModifiedBy,
          lastModifiedDate = lastModifiedDate,
          id = customerId,
        )

      // When
      val result = classUnderTest.toModel(customerEntity)

      // Then
      assertNotNull(result)
      assertEquals(customerId, result.id)
      assertEquals("José María & O'Connor-Smith Ltd.", result.name)
      assertEquals("ES-B12345678", result.taxIdentifier)
      assertEquals(createdDate, result.createdDate)
      assertEquals(createdBy, result.createdBy)
      assertEquals(lastModifiedBy, result.lastModifiedBy)
      assertEquals(lastModifiedDate, result.lastModifiedDate)
    }

    @Test
    fun `should convert CustomerEntity with long names`() {
      // Given
      val customerId = UUID.randomUUID()
      val longCompanyName = "A".repeat(200) + " Corporation"
      val longTaxId = "TAX-" + "1".repeat(50)

      val customerEntity =
        CustomerEntity(
          name = longCompanyName,
          taxIdentifier = longTaxId,
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = customerId,
        )

      // When
      val result = classUnderTest.toModel(customerEntity)

      // Then
      assertNotNull(result)
      assertEquals(customerId, result.id)
      assertEquals(longCompanyName, result.name)
      assertEquals(longTaxId, result.taxIdentifier)
    }

    @Test
    fun `should convert CustomerEntity with null optional fields`() {
      // Given
      val customerId = UUID.randomUUID()
      val customerEntity =
        CustomerEntity(
          name = "Basic Company",
          taxIdentifier = "TAX123",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = customerId,
        )

      // When
      val result = classUnderTest.toModel(customerEntity)

      // Then
      assertNotNull(result)
      assertEquals(customerId, result.id)
      assertEquals("Basic Company", result.name)
      assertEquals("TAX123", result.taxIdentifier)
      assertNull(result.createdDate)
      assertNull(result.createdBy)
      assertNull(result.lastModifiedBy)
      assertNull(result.lastModifiedDate)
    }
  }

  @Nested
  inner class ToUpdateModelTest {

    @Test
    fun `should convert CustomerEntity to UpdateModel with all fields`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "Global Industries",
          taxIdentifier = "GI-987654321",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      // When
      val result = classUnderTest.toUpdateModel(customerEntity)

      // Then
      assertNotNull(result)
      assertEquals("Global Industries", result.name)
      assertEquals("GI-987654321", result.taxIdentifier)
    }

    @Test
    fun `should convert CustomerEntity to UpdateModel with special characters`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "Café & Résumé Solutions S.A.",
          taxIdentifier = "FR-123456789",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      // When
      val result = classUnderTest.toUpdateModel(customerEntity)

      // Then
      assertNotNull(result)
      assertEquals("Café & Résumé Solutions S.A.", result.name)
      assertEquals("FR-123456789", result.taxIdentifier)
    }

    @Test
    fun `should convert CustomerEntity to UpdateModel with long values`() {
      // Given
      val longName = "Very Long Company Name ".repeat(10) + "Inc."
      val longTaxId = "LONG-TAX-ID-" + "X".repeat(100)
      val customerEntity =
        CustomerEntity(
          name = longName,
          taxIdentifier = longTaxId,
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      // When
      val result = classUnderTest.toUpdateModel(customerEntity)

      // Then
      assertNotNull(result)
      assertEquals(longName, result.name)
      assertEquals(longTaxId, result.taxIdentifier)
    }

    @Test
    fun `should convert CustomerEntity to UpdateModel with minimal data`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "ABC",
          taxIdentifier = "123",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      // When
      val result = classUnderTest.toUpdateModel(customerEntity)

      // Then
      assertNotNull(result)
      assertEquals("ABC", result.name)
      assertEquals("123", result.taxIdentifier)
    }
  }

  @Nested
  inner class UpdateEntityTest {

    @Test
    fun `should update CustomerEntity with all fields`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "Old Company Name",
          taxIdentifier = "OLD-TAX-123",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      val updateModel =
        UpdateCustomerModel(name = "New Company Name", taxIdentifier = "NEW-TAX-456")

      // When
      classUnderTest.updateEntity(customerEntity, updateModel)

      // Then
      assertEquals("New Company Name", customerEntity.name)
      assertEquals("NEW-TAX-456", customerEntity.taxIdentifier)
    }

    @Test
    fun `should update CustomerEntity with null fields`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "Original Company",
          taxIdentifier = "ORIG-TAX-789",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      val updateModel = UpdateCustomerModel(name = null, taxIdentifier = null)

      // When
      classUnderTest.updateEntity(customerEntity, updateModel)

      // Then - Original values should remain unchanged when update model has nulls
      assertEquals("Original Company", customerEntity.name)
      assertEquals("ORIG-TAX-789", customerEntity.taxIdentifier)
    }

    @Test
    fun `should update CustomerEntity with special characters`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "Simple Corp",
          taxIdentifier = "SIMPLE-123",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      val updateModel =
        UpdateCustomerModel(
          name = "José María & O'Connor-Smith Ltd.",
          taxIdentifier = "ES-B98765432",
        )

      // When
      classUnderTest.updateEntity(customerEntity, updateModel)

      // Then
      assertEquals("José María & O'Connor-Smith Ltd.", customerEntity.name)
      assertEquals("ES-B98765432", customerEntity.taxIdentifier)
    }

    @Test
    fun `should update CustomerEntity with long values`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "Short Name",
          taxIdentifier = "SHORT",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      val longName = "Very Long Company Name That Spans Multiple Words ".repeat(5) + "Corporation"
      val longTaxId = "VERY-LONG-TAX-IDENTIFIER-" + "9".repeat(100)
      val updateModel = UpdateCustomerModel(name = longName, taxIdentifier = longTaxId)

      // When
      classUnderTest.updateEntity(customerEntity, updateModel)

      // Then
      assertEquals(longName, customerEntity.name)
      assertEquals(longTaxId, customerEntity.taxIdentifier)
    }

    @Test
    fun `should update CustomerEntity partially`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "Original Name",
          taxIdentifier = "ORIGINAL-TAX",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      val updateModel = UpdateCustomerModel(name = "Updated Name", taxIdentifier = null)

      // When
      classUnderTest.updateEntity(customerEntity, updateModel)

      // Then - Only name should be updated, tax identifier should remain unchanged
      assertEquals("Updated Name", customerEntity.name)
      assertEquals("ORIGINAL-TAX", customerEntity.taxIdentifier)
    }

    @Test
    fun `should update CustomerEntity tax identifier only`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "Company Name",
          taxIdentifier = "OLD-TAX",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      val updateModel = UpdateCustomerModel(name = null, taxIdentifier = "NEW-TAX-ID")

      // When
      classUnderTest.updateEntity(customerEntity, updateModel)

      // Then - Only tax identifier should be updated, name should remain unchanged
      assertEquals("Company Name", customerEntity.name)
      assertEquals("NEW-TAX-ID", customerEntity.taxIdentifier)
    }

    @Test
    fun `should update CustomerEntity from short to long values`() {
      // Given
      val customerEntity =
        CustomerEntity(
          name = "A",
          taxIdentifier = "1",
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      val longName = "Extremely Long Company Name With Many Words And Descriptions"
      val longTaxId = "EXTREMELY-LONG-TAX-IDENTIFIER-WITH-MANY-SEGMENTS-123456789"
      val updateModel = UpdateCustomerModel(name = longName, taxIdentifier = longTaxId)

      // When
      classUnderTest.updateEntity(customerEntity, updateModel)

      // Then
      assertEquals(longName, customerEntity.name)
      assertEquals(longTaxId, customerEntity.taxIdentifier)
    }

    @Test
    fun `should update CustomerEntity from long to short values`() {
      // Given
      val longOriginalName = "Very Long Original Company Name With Many Words"
      val longOriginalTaxId = "VERY-LONG-ORIGINAL-TAX-IDENTIFIER-123456789"
      val customerEntity =
        CustomerEntity(
          name = longOriginalName,
          taxIdentifier = longOriginalTaxId,
          createdBy = null,
          createdDate = null,
          lastModifiedBy = null,
          lastModifiedDate = null,
          id = null,
        )

      val updateModel = UpdateCustomerModel(name = "Short Co.", taxIdentifier = "TAX123")

      // When
      classUnderTest.updateEntity(customerEntity, updateModel)

      // Then
      assertEquals("Short Co.", customerEntity.name)
      assertEquals("TAX123", customerEntity.taxIdentifier)
    }
  }
}
