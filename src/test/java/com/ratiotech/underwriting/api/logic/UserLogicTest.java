package com.ratiotech.underwriting.api.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.ratiotech.underwriting.api.controllers.requests.CreateUserRequest;
import com.ratiotech.underwriting.api.controllers.requests.UpdateUserModel;
import com.ratiotech.underwriting.api.controllers.responses.User;
import com.ratiotech.underwriting.api.entities.UserEntity;
import com.ratiotech.underwriting.api.logic.translators.UserTranslator;
import com.ratiotech.underwriting.api.repositories.UserRepository;
import com.ratiotech.underwriting.api.shared.constants.Constants;
import com.ratiotech.underwriting.api.shared.exceptions.BadRequestException;
import com.ratiotech.underwriting.api.shared.exceptions.NotFoundException;
import com.ratiotech.underwriting.api.shared.logic.JsonPatchManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for UserLogic. Tests all business logic methods with various scenarios. */
@ExtendWith(MockitoExtension.class)
class UserLogicTest {

  @Mock private UserRepository userRepository;
  @Mock private UserTranslator userTranslator;
  @Mock private JsonPatchManager jsonPatchManager;

  private UserLogic classUnderTest;

  @BeforeEach
  void setUp() {
    classUnderTest = new UserLogic(userRepository, userTranslator, jsonPatchManager);
  }

  @Nested
  class CreateUserTest {

    @Test
    void shouldCreateUserSuccessfully() {
      // Given
      var request =
          new CreateUserRequest(
              "John", "Doe", "john.doe@example.com", "Software Engineer", "+1-555-123-4567");

      var savedUserId = UUID.randomUUID();
      var savedUserEntity =
          UserEntity.builder()
              .id(savedUserId)
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .createdBy(Constants.INSTANCE.getSYSTEM_IDENTITY())
              .lastModifiedBy(Constants.INSTANCE.getSYSTEM_IDENTITY())
              .build();

      when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
      when(userRepository.save(any(UserEntity.class))).thenReturn(savedUserEntity);

      // When
      var result = classUnderTest.createUser(request);

      // Then
      assertNotNull(result);
      assertEquals(savedUserId, result);
      verify(userRepository).existsByEmail("john.doe@example.com");
      verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldCreateUserWithMinimalData() {
      // Given
      var request = new CreateUserRequest("Jane", "Smith", "jane.smith@example.com", null, null);

      var savedUserId = UUID.randomUUID();
      var savedUserEntity =
          UserEntity.builder()
              .id(savedUserId)
              .firstName("Jane")
              .lastName("Smith")
              .email("jane.smith@example.com")
              .title(null)
              .phoneNumber(null)
              .createdBy(Constants.INSTANCE.getSYSTEM_IDENTITY())
              .lastModifiedBy(Constants.INSTANCE.getSYSTEM_IDENTITY())
              .build();

      when(userRepository.existsByEmail("jane.smith@example.com")).thenReturn(false);
      when(userRepository.save(any(UserEntity.class))).thenReturn(savedUserEntity);

      // When
      var result = classUnderTest.createUser(request);

      // Then
      assertNotNull(result);
      assertEquals(savedUserId, result);
      verify(userRepository).existsByEmail("jane.smith@example.com");
      verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenEmailAlreadyExists() {
      // Given
      var request =
          new CreateUserRequest(
              "John", "Doe", "existing.user@example.com", "Software Engineer", "+1-555-123-4567");

      when(userRepository.existsByEmail("existing.user@example.com")).thenReturn(true);

      // When & Then
      var exception =
          assertThrows(BadRequestException.class, () -> classUnderTest.createUser(request));

      assertEquals(
          "User with email 'existing.user@example.com' already exists", exception.getMessage());
      verify(userRepository).existsByEmail("existing.user@example.com");
      verify(userRepository, never()).save(any(UserEntity.class));
    }
  }

  @Nested
  class GetUserByIdTest {

    @Test
    void shouldGetUserByIdSuccessfully() {
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

      var expectedUser =
          new User(
              userId,
              "john.doe@example.com",
              "John",
              "Doe",
              "John Doe",
              "Software Engineer",
              "+1-555-123-4567");

      when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
      when(userTranslator.toModel(userEntity)).thenReturn(expectedUser);

      // When
      var result = classUnderTest.getUserById(userId);

      // Then
      assertNotNull(result);
      assertEquals(expectedUser, result);
      verify(userRepository).findById(userId);
      verify(userTranslator).toModel(userEntity);
    }

    @Test
    void shouldGetUserByIdWithMinimalData() {
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

      var expectedUser =
          new User(userId, "jane.smith@example.com", "Jane", "Smith", "Jane Smith", null, null);

      when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
      when(userTranslator.toModel(userEntity)).thenReturn(expectedUser);

      // When
      var result = classUnderTest.getUserById(userId);

      // Then
      assertNotNull(result);
      assertEquals(expectedUser, result);
      verify(userRepository).findById(userId);
      verify(userTranslator).toModel(userEntity);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
      // Given
      var userId = UUID.randomUUID();
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // When & Then
      var exception =
          assertThrows(NotFoundException.class, () -> classUnderTest.getUserById(userId));

      assertEquals("User not found with id: " + userId, exception.getMessage());
      verify(userRepository).findById(userId);
      verify(userTranslator, never()).toModel(any(UserEntity.class));
    }
  }

  @Nested
  class GetAllUsersTest {

    @Test
    void shouldGetAllUsersSuccessfully() {
      // Given
      var user1Entity =
          UserEntity.builder()
              .id(UUID.randomUUID())
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .build();

      var user2Entity =
          UserEntity.builder()
              .id(UUID.randomUUID())
              .firstName("Jane")
              .lastName("Smith")
              .email("jane.smith@example.com")
              .title("Product Manager")
              .phoneNumber("+1-555-987-6543")
              .build();

      var user1 =
          new User(
              user1Entity.getId(),
              "john.doe@example.com",
              "John",
              "Doe",
              "John Doe",
              "Software Engineer",
              "+1-555-123-4567");

      var user2 =
          new User(
              user2Entity.getId(),
              "jane.smith@example.com",
              "Jane",
              "Smith",
              "Jane Smith",
              "Product Manager",
              "+1-555-987-6543");

      when(userRepository.findAll()).thenReturn(Arrays.asList(user1Entity, user2Entity));
      when(userTranslator.toModel(user1Entity)).thenReturn(user1);
      when(userTranslator.toModel(user2Entity)).thenReturn(user2);

      // When
      var result = classUnderTest.getAllUsers();

      // Then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertTrue(result.contains(user1));
      assertTrue(result.contains(user2));
      verify(userRepository).findAll();
      verify(userTranslator).toModel(user1Entity);
      verify(userTranslator).toModel(user2Entity);
    }

    @Test
    void shouldGetAllUsersWhenEmpty() {
      // Given
      when(userRepository.findAll()).thenReturn(Collections.emptyList());

      // When
      var result = classUnderTest.getAllUsers();

      // Then
      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(userRepository).findAll();
      verify(userTranslator, never()).toModel(any(UserEntity.class));
    }

    @Test
    void shouldGetAllUsersWithSingleUser() {
      // Given
      var userEntity =
          UserEntity.builder()
              .id(UUID.randomUUID())
              .firstName("Alice")
              .lastName("Johnson")
              .email("alice.johnson@example.com")
              .title("Designer")
              .phoneNumber("+1-555-111-2222")
              .build();

      var user =
          new User(
              userEntity.getId(),
              "alice.johnson@example.com",
              "Alice",
              "Johnson",
              "Alice Johnson",
              "Designer",
              "+1-555-111-2222");

      when(userRepository.findAll()).thenReturn(List.of(userEntity));
      when(userTranslator.toModel(userEntity)).thenReturn(user);

      // When
      var result = classUnderTest.getAllUsers();

      // Then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(user, result.get(0));
      verify(userRepository).findAll();
      verify(userTranslator).toModel(userEntity);
    }
  }

  @Nested
  class PatchUserTest {

    @Test
    void shouldPatchUserSuccessfully() throws IOException, JsonPatchException {
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

      var updateModel =
          new UpdateUserModel(
              "John", "Doe", "john.doe@example.com", "Software Engineer", "+1-555-123-4567");

      var patchedUpdateModel =
          new UpdateUserModel(
              "Jane", "Smith", "jane.smith@example.com", "Senior Engineer", "+1-555-987-6543");

      var updatedEntity =
          UserEntity.builder()
              .id(userId)
              .firstName("Jane")
              .lastName("Smith")
              .email("jane.smith@example.com")
              .title("Senior Engineer")
              .phoneNumber("+1-555-987-6543")
              .build();

      var expectedUser =
          new User(
              userId,
              "jane.smith@example.com",
              "Jane",
              "Smith",
              "Jane Smith",
              "Senior Engineer",
              "+1-555-987-6543");

      // Create a real JsonPatch object for testing
      var objectMapper = new ObjectMapper();
      var patchJson = "[{\"op\": \"replace\", \"path\": \"/firstName\", \"value\": \"Jane\"}]";
      var jsonPatch = JsonPatch.fromJson(objectMapper.readTree(patchJson));

      when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
      when(userTranslator.toUpdateModel(userEntity)).thenReturn(updateModel);
      when(jsonPatchManager.applyPatch(eq(jsonPatch), eq(updateModel), eq(UpdateUserModel.class)))
          .thenReturn(patchedUpdateModel);
      when(userRepository.save(userEntity)).thenReturn(updatedEntity);
      when(userTranslator.toModel(updatedEntity)).thenReturn(expectedUser);

      // When
      var result = classUnderTest.patchUser(userId, jsonPatch);

      // Then
      assertNotNull(result);
      assertEquals(expectedUser, result);
      verify(userRepository).findById(userId);
      verify(userTranslator).toUpdateModel(userEntity);
      verify(jsonPatchManager)
          .applyPatch(eq(jsonPatch), eq(updateModel), eq(UpdateUserModel.class));
      verify(userTranslator).updateEntity(userEntity, patchedUpdateModel);
      verify(userRepository).save(userEntity);
      verify(userTranslator).toModel(updatedEntity);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPatchingNonExistentUser()
        throws IOException, JsonPatchException {
      // Given
      var userId = UUID.randomUUID();
      var objectMapper = new ObjectMapper();
      var patchJson = "[{\"op\": \"replace\", \"path\": \"/firstName\", \"value\": \"Jane\"}]";
      var jsonPatch = JsonPatch.fromJson(objectMapper.readTree(patchJson));

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // When & Then
      var exception =
          assertThrows(NotFoundException.class, () -> classUnderTest.patchUser(userId, jsonPatch));

      assertEquals("User not found with id: " + userId, exception.getMessage());
      verify(userRepository).findById(userId);
      verify(userTranslator, never()).toUpdateModel(any(UserEntity.class));
      verify(jsonPatchManager, never()).applyPatch(any(), any(), any());
      verify(userTranslator, never()).updateEntity(any(), any());
      verify(userRepository, never()).save(any(UserEntity.class));
      verify(userTranslator, never()).toModel(any(UserEntity.class));
    }
  }

  @Nested
  class DeleteUserTest {

    @Test
    void shouldDeleteUserSuccessfully() {
      // Given
      var userId = UUID.randomUUID();
      when(userRepository.existsById(userId)).thenReturn(true);

      // When
      classUnderTest.deleteUser(userId);

      // Then
      verify(userRepository).existsById(userId);
      verify(userRepository).deleteById(userId);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentUser() {
      // Given
      var userId = UUID.randomUUID();
      when(userRepository.existsById(userId)).thenReturn(false);

      // When & Then
      var exception =
          assertThrows(NotFoundException.class, () -> classUnderTest.deleteUser(userId));

      assertEquals("User not found with id: " + userId, exception.getMessage());
      verify(userRepository).existsById(userId);
      verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void shouldDeleteUserWithSpecialId() {
      // Given
      var userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
      when(userRepository.existsById(userId)).thenReturn(true);

      // When
      classUnderTest.deleteUser(userId);

      // Then
      verify(userRepository).existsById(userId);
      verify(userRepository).deleteById(userId);
    }
  }
}
