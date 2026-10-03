package com.ratiotech.underwriting.api.logic.translators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ratiotech.underwriting.api.controllers.requests.UpdateUserModel;
import com.ratiotech.underwriting.api.entities.UserEntity;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for UserTranslator. Tests all translation methods with various data scenarios. */
class UserTranslatorTest {

  private UserTranslator classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new UserTranslator();
  }

  @Nested
  class ToModelTest {

    @Test
    void shouldConvertUserEntityToUserWithAllFields() {
      // Given
      var userId = UUID.randomUUID();
      var userEntity =
          UserEntity.builder()
              .id(userId)
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .build();

      // When
      var result = classUnderTest.toModel(userEntity);

      // Then
      assertNotNull(result);
      assertEquals(userId, result.id());
      assertEquals("john.doe@example.com", result.email());
      assertEquals("John", result.firstName());
      assertEquals("Doe", result.lastName());
      assertEquals("John Doe", result.fullName());
      assertEquals("Software Engineer", result.title());
      assertEquals("+1-555-123-4567", result.phoneNumber());
    }

    @Test
    void shouldConvertUserEntityToUserWithRequiredFieldsOnly() {
      // Given
      var userId = UUID.randomUUID();
      var userEntity =
          UserEntity.builder()
              .id(userId)
              .firstName("Jane")
              .lastName("Smith")
              .email("jane.smith@example.com")
              .title(null)
              .phoneNumber(null)
              .build();

      // When
      var result = classUnderTest.toModel(userEntity);

      // Then
      assertNotNull(result);
      assertEquals(userId, result.id());
      assertEquals("jane.smith@example.com", result.email());
      assertEquals("Jane", result.firstName());
      assertEquals("Smith", result.lastName());
      assertEquals("Jane Smith", result.fullName());
      assertNull(result.title());
      assertNull(result.phoneNumber());
    }

    @Test
    void shouldConvertUserEntityWithSpecialCharacters() {
      // Given
      var userId = UUID.randomUUID();
      var userEntity =
          UserEntity.builder()
              .id(userId)
              .firstName("José María")
              .lastName("O'Connor-Smith")
              .email("jose.maria@example.com")
              .title("Señor Developer")
              .phoneNumber("+34-666-123-456")
              .build();

      // When
      var result = classUnderTest.toModel(userEntity);

      // Then
      assertNotNull(result);
      assertEquals(userId, result.id());
      assertEquals("jose.maria@example.com", result.email());
      assertEquals("José María", result.firstName());
      assertEquals("O'Connor-Smith", result.lastName());
      assertEquals("José María O'Connor-Smith", result.fullName());
      assertEquals("Señor Developer", result.title());
      assertEquals("+34-666-123-456", result.phoneNumber());
    }

    @Test
    void shouldConvertUserEntityWithEmptyOptionalFields() {
      // Given
      var userId = UUID.randomUUID();
      var userEntity =
          UserEntity.builder()
              .id(userId)
              .firstName("Bob")
              .lastName("Wilson")
              .email("bob.wilson@example.com")
              .title("")
              .phoneNumber("")
              .build();

      // When
      var result = classUnderTest.toModel(userEntity);

      // Then
      assertNotNull(result);
      assertEquals(userId, result.id());
      assertEquals("bob.wilson@example.com", result.email());
      assertEquals("Bob", result.firstName());
      assertEquals("Wilson", result.lastName());
      assertEquals("Bob Wilson", result.fullName());
      assertEquals("", result.title());
      assertEquals("", result.phoneNumber());
    }

    @Test
    void shouldConvertUserEntityWithLongNames() {
      // Given
      var userId = UUID.randomUUID();
      var longFirstName = "A".repeat(100);
      var longLastName = "B".repeat(100);
      var userEntity =
          UserEntity.builder()
              .id(userId)
              .firstName(longFirstName)
              .lastName(longLastName)
              .email("long.name@example.com")
              .title("Engineer with a very long title that spans multiple words")
              .phoneNumber("+1-555-999-8888")
              .build();

      // When
      var result = classUnderTest.toModel(userEntity);

      // Then
      assertNotNull(result);
      assertEquals(userId, result.id());
      assertEquals("long.name@example.com", result.email());
      assertEquals(longFirstName, result.firstName());
      assertEquals(longLastName, result.lastName());
      assertEquals(longFirstName + " " + longLastName, result.fullName());
      assertEquals("Engineer with a very long title that spans multiple words", result.title());
      assertEquals("+1-555-999-8888", result.phoneNumber());
    }
  }

  @Nested
  class ToUpdateModelTest {

    @Test
    void shouldConvertUserEntityToUpdateModelWithAllFields() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .build();

      // When
      var result = classUnderTest.toUpdateModel(userEntity);

      // Then
      assertNotNull(result);
      assertEquals("John", result.firstName());
      assertEquals("Doe", result.lastName());
      assertEquals("john.doe@example.com", result.email());
      assertEquals("Software Engineer", result.title());
      assertEquals("+1-555-123-4567", result.phoneNumber());
    }

    @Test
    void shouldConvertUserEntityToUpdateModelWithRequiredFieldsOnly() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("Jane")
              .lastName("Smith")
              .email("jane.smith@example.com")
              .title(null)
              .phoneNumber(null)
              .build();

      // When
      var result = classUnderTest.toUpdateModel(userEntity);

      // Then
      assertNotNull(result);
      assertEquals("Jane", result.firstName());
      assertEquals("Smith", result.lastName());
      assertEquals("jane.smith@example.com", result.email());
      assertNull(result.title());
      assertNull(result.phoneNumber());
    }

    @Test
    void shouldConvertUserEntityToUpdateModelWithSpecialCharacters() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("José María")
              .lastName("O'Connor-Smith")
              .email("jose.maria@example.com")
              .title("Señor Developer")
              .phoneNumber("+34-666-123-456")
              .build();

      // When
      var result = classUnderTest.toUpdateModel(userEntity);

      // Then
      assertNotNull(result);
      assertEquals("José María", result.firstName());
      assertEquals("O'Connor-Smith", result.lastName());
      assertEquals("jose.maria@example.com", result.email());
      assertEquals("Señor Developer", result.title());
      assertEquals("+34-666-123-456", result.phoneNumber());
    }

    @Test
    void shouldConvertUserEntityToUpdateModelWithEmptyOptionalFields() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("Bob")
              .lastName("Wilson")
              .email("bob.wilson@example.com")
              .title("")
              .phoneNumber("")
              .build();

      // When
      var result = classUnderTest.toUpdateModel(userEntity);

      // Then
      assertNotNull(result);
      assertEquals("Bob", result.firstName());
      assertEquals("Wilson", result.lastName());
      assertEquals("bob.wilson@example.com", result.email());
      assertEquals("", result.title());
      assertEquals("", result.phoneNumber());
    }
  }

  @Nested
  class UpdateEntityTest {

    @Test
    void shouldUpdateUserEntityWithAllFields() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .build();

      var updateModel =
          new UpdateUserModel(
              "Jane",
              "Smith",
              "jane.smith@example.com",
              "Senior Software Engineer",
              "+1-555-987-6543");

      // When
      classUnderTest.updateEntity(userEntity, updateModel);

      // Then
      assertEquals("Jane", userEntity.getFirstName());
      assertEquals("Smith", userEntity.getLastName());
      assertEquals("jane.smith@example.com", userEntity.getEmail());
      assertEquals("Senior Software Engineer", userEntity.getTitle());
      assertEquals("+1-555-987-6543", userEntity.getPhoneNumber());
    }

    @Test
    void shouldUpdateUserEntityWithNullOptionalFields() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .build();

      var updateModel = new UpdateUserModel("Jane", "Smith", "jane.smith@example.com", null, null);

      // When
      classUnderTest.updateEntity(userEntity, updateModel);

      // Then
      assertEquals("Jane", userEntity.getFirstName());
      assertEquals("Smith", userEntity.getLastName());
      assertEquals("jane.smith@example.com", userEntity.getEmail());
      assertNull(userEntity.getTitle());
      assertNull(userEntity.getPhoneNumber());
    }

    @Test
    void shouldUpdateUserEntityWithSpecialCharacters() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .build();

      var updateModel =
          new UpdateUserModel(
              "José María",
              "O'Connor-Smith",
              "jose.maria@example.com",
              "Señor Developer",
              "+34-666-123-456");

      // When
      classUnderTest.updateEntity(userEntity, updateModel);

      // Then
      assertEquals("José María", userEntity.getFirstName());
      assertEquals("O'Connor-Smith", userEntity.getLastName());
      assertEquals("jose.maria@example.com", userEntity.getEmail());
      assertEquals("Señor Developer", userEntity.getTitle());
      assertEquals("+34-666-123-456", userEntity.getPhoneNumber());
    }

    @Test
    void shouldUpdateUserEntityWithEmptyStrings() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .build();

      var updateModel = new UpdateUserModel("Jane", "Smith", "jane.smith@example.com", "", "");

      // When
      classUnderTest.updateEntity(userEntity, updateModel);

      // Then
      assertEquals("Jane", userEntity.getFirstName());
      assertEquals("Smith", userEntity.getLastName());
      assertEquals("jane.smith@example.com", userEntity.getEmail());
      assertEquals("", userEntity.getTitle());
      assertEquals("", userEntity.getPhoneNumber());
    }

    @Test
    void shouldUpdateUserEntityPartially() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .build();

      var updateModel =
          new UpdateUserModel(
              // Only change first name
              "Jane",
              // Keep same last name
              "Doe",
              // Keep same email
              "john.doe@example.com",
              // Keep same title
              "Software Engineer",
              // Keep same phone
              "+1-555-123-4567");

      // When
      classUnderTest.updateEntity(userEntity, updateModel);

      // Then
      assertEquals("Jane", userEntity.getFirstName());
      assertEquals("Doe", userEntity.getLastName());
      assertEquals("john.doe@example.com", userEntity.getEmail());
      assertEquals("Software Engineer", userEntity.getTitle());
      assertEquals("+1-555-123-4567", userEntity.getPhoneNumber());
    }

    @Test
    void shouldUpdateUserEntityFromNullToValues() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title(null)
              .phoneNumber(null)
              .build();

      var updateModel =
          new UpdateUserModel(
              "John", "Doe", "john.doe@example.com", "Software Engineer", "+1-555-123-4567");

      // When
      classUnderTest.updateEntity(userEntity, updateModel);

      // Then
      assertEquals("John", userEntity.getFirstName());
      assertEquals("Doe", userEntity.getLastName());
      assertEquals("john.doe@example.com", userEntity.getEmail());
      assertEquals("Software Engineer", userEntity.getTitle());
      assertEquals("+1-555-123-4567", userEntity.getPhoneNumber());
    }

    @Test
    void shouldUpdateUserEntityFromValuesToNull() {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .build();

      var updateModel = new UpdateUserModel("John", "Doe", "john.doe@example.com", null, null);

      // When
      classUnderTest.updateEntity(userEntity, updateModel);

      // Then
      assertEquals("John", userEntity.getFirstName());
      assertEquals("Doe", userEntity.getLastName());
      assertEquals("john.doe@example.com", userEntity.getEmail());
      assertNull(userEntity.getTitle());
      assertNull(userEntity.getPhoneNumber());
    }
  }
}
