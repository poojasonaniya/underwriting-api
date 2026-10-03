package com.ratiotech.underwriting.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratiotech.underwriting.api.controllers.requests.CreateUserRequest;
import com.ratiotech.underwriting.api.controllers.responses.User;
import com.ratiotech.underwriting.api.entities.UserEntity;
import com.ratiotech.underwriting.api.shared.IntegrationTestBase;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

/**
 * Integration tests for UserController. Tests the createUser endpoint with real database
 * interactions.
 */
public class UserControllerIntegrationTest extends IntegrationTestBase {

  @Autowired private ObjectMapper objectMapper;
  @Autowired private JacksonTester<User> userJacksonTester;
  @Autowired private JacksonTester<List<User>> userListJacksonTester;
  @Autowired private JacksonTester<ProblemDetail> problemDetailJacksonTester;

  @Override
  protected void beforeEach() {
    // No additional setup needed for User tests
  }

  @Nested
  class CreateUserTest {

    @Test
    void shouldSuccessfullyCreateUserWithAllFields() throws Exception {
      // Given
      var request =
          new CreateUserRequest(
              "John", "Doe", "john.doe@example.com", "Software Engineer", "+1-555-123-4567");

      // When & Then
      var result =
          mockMvc
              .perform(
                  post(USERS_PATH)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isCreated())
              .andExpect(header().exists("Location"))
              .andReturn();

      // Verify Location header contains valid UUID
      var locationHeader = result.getResponse().getHeader("Location");
      assertNotNull(locationHeader);
      assertTrue(locationHeader.contains("/v1/users/"));

      // Extract UUID from location header and verify it's valid
      var userId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
      assertNotNull(UUID.fromString(userId)); // Should not throw exception

      // Verify user was created in database
      var savedUser = userRepository.findById(UUID.fromString(userId));
      assertTrue(savedUser.isPresent());
      assertEquals("John", savedUser.get().getFirstName());
      assertEquals("Doe", savedUser.get().getLastName());
      assertEquals("john.doe@example.com", savedUser.get().getEmail());
      assertEquals("Software Engineer", savedUser.get().getTitle());
      assertEquals("+1-555-123-4567", savedUser.get().getPhoneNumber());
    }

    @Test
    void shouldSuccessfullyCreateUserWithRequiredFieldsOnly() throws Exception {
      // Given
      var request = new CreateUserRequest("Jane", "Smith", "jane.smith@example.com", null, null);

      // When & Then
      var result =
          mockMvc
              .perform(
                  post(USERS_PATH)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isCreated())
              .andExpect(header().exists("Location"))
              .andReturn();

      // Verify user was created in database
      var locationHeader = result.getResponse().getHeader("Location");
      assertNotNull(locationHeader);
      var userId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
      var savedUser = userRepository.findById(UUID.fromString(userId));

      assertTrue(savedUser.isPresent());
      assertEquals("Jane", savedUser.get().getFirstName());
      assertEquals("Smith", savedUser.get().getLastName());
      assertEquals("jane.smith@example.com", savedUser.get().getEmail());
      assertNull(savedUser.get().getTitle());
      assertNull(savedUser.get().getPhoneNumber());
    }

    @Test
    void shouldReturn400WhenFirstNameIsEmpty() throws Exception {
      // Given
      var request = new CreateUserRequest("", "Doe", "john.doe@example.com", "Engineer", null);

      // When & Then
      var result =
          mockMvc
              .perform(
                  post(USERS_PATH)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isBadRequest())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
      assertTrue(problem.getDetail().contains("'firstName' is required"));
    }

    @Test
    void shouldReturn400WhenFirstNameIsNull() throws Exception {
      // Given
      var request = new CreateUserRequest(null, "Doe", "john.doe@example.com", "Engineer", null);

      // When & Then
      var result =
          mockMvc
              .perform(
                  post(USERS_PATH)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isBadRequest())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
      assertTrue(problem.getDetail().contains("'firstName' is required"));
    }

    @Test
    void shouldReturn400WhenLastNameIsEmpty() throws Exception {
      // Given
      var request = new CreateUserRequest("John", "", "john.doe@example.com", "Engineer", null);

      // When & Then
      var result =
          mockMvc
              .perform(
                  post(USERS_PATH)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isBadRequest())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
      assertTrue(problem.getDetail().contains("'lastName' is required"));
    }

    @Test
    void shouldReturn400WhenLastNameIsNull() throws Exception {
      // Given
      var request = new CreateUserRequest("John", null, "john.doe@example.com", "Engineer", null);

      // When & Then
      var result =
          mockMvc
              .perform(
                  post(USERS_PATH)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isBadRequest())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
      assertTrue(problem.getDetail().contains("'lastName' is required"));
    }

    @Test
    void shouldReturn400WhenEmailIsEmpty() throws Exception {
      // Given
      var request = new CreateUserRequest("John", "Doe", "", "Engineer", null);

      // When & Then
      var result =
          mockMvc
              .perform(
                  post(USERS_PATH)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isBadRequest())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
      assertTrue(problem.getDetail().contains("'email' is required"));
    }

    @Test
    void shouldReturn400WhenEmailIsNull() throws Exception {
      // Given
      var request = new CreateUserRequest("John", "Doe", null, "Engineer", null);

      // When & Then
      var result =
          mockMvc
              .perform(
                  post(USERS_PATH)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isBadRequest())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
      assertTrue(problem.getDetail().contains("'email' is required"));
    }

    @Test
    void shouldReturn400WhenTryingToCreateUserWithDuplicateEmail() throws Exception {
      // Given - Create first user
      var existingUser =
          UserEntity.builder()
              .firstName("Existing")
              .lastName("User")
              .email("duplicate@example.com")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      userRepository.save(existingUser);

      var request = new CreateUserRequest("John", "Doe", "duplicate@example.com", "Engineer", null);

      // When & Then
      var result =
          mockMvc
              .perform(
                  post(USERS_PATH)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isBadRequest())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
      assertEquals("User with email 'duplicate@example.com' already exists", problem.getDetail());
    }

    @Test
    void shouldReturn400WhenRequestBodyIsEmpty() throws Exception {
      // When & Then
      mockMvc
          .perform(post(USERS_PATH).contentType(MediaType.APPLICATION_JSON).content(""))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenRequestBodyIsMalformedJson() throws Exception {
      // Given
      var malformedJson = "{\"firstName\": \"John\", \"lastName\": }";

      // When & Then
      mockMvc
          .perform(post(USERS_PATH).contentType(MediaType.APPLICATION_JSON).content(malformedJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn415WhenContentTypeIsNotJson() throws Exception {
      // Given
      var request = new CreateUserRequest("John", "Doe", "john.doe@example.com", "Engineer", null);

      // When & Then
      mockMvc
          .perform(
              post(USERS_PATH)
                  .contentType(MediaType.TEXT_PLAIN)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnsupportedMediaType());
    }
  }

  @Nested
  class GetAllUsersTest {

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() throws Exception {
      // When & Then
      var result = mockMvc.perform(get(USERS_PATH)).andExpect(status().isOk()).andReturn();
      var users = userListJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(0, users.size());
    }

    @Test
    void shouldReturnSingleUserWhenOneUserExists() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      // When & Then
      var result = mockMvc.perform(get(USERS_PATH)).andExpect(status().isOk()).andReturn();
      var users = userListJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(1, users.size());
      var u = users.get(0);
      assertEquals(savedUser.getId(), u.id());
      assertEquals("John", u.firstName());
      assertEquals("Doe", u.lastName());
      assertEquals("john.doe@example.com", u.email());
      assertEquals("John Doe", u.fullName());
      assertEquals("Software Engineer", u.title());
      assertEquals("+1-555-123-4567", u.phoneNumber());
    }

    @Test
    void shouldReturnMultipleUsers() throws Exception {
      // Given
      var user1 =
          UserEntity.builder()
              .firstName("Alice")
              .lastName("Johnson")
              .email("alice.johnson@example.com")
              .title("Product Manager")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();

      var user2 =
          UserEntity.builder()
              .firstName("Bob")
              .lastName("Wilson")
              .email("bob.wilson@example.com")
              .title("Designer")
              .phoneNumber("+1-555-987-6543")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();

      var user3 =
          UserEntity.builder()
              .firstName("Charlie")
              .lastName("Brown")
              .email("charlie.brown@example.com")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();

      var savedUser1 = userRepository.save(user1);
      var savedUser2 = userRepository.save(user2);
      var savedUser3 = userRepository.save(user3);

      // When & Then
      var result = mockMvc.perform(get(USERS_PATH)).andExpect(status().isOk()).andReturn();
      var users = userListJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(3, users.size());
      var ids = users.stream().map(User::id).collect(java.util.stream.Collectors.toSet());
      assertTrue(ids.contains(savedUser1.getId()));
      assertTrue(ids.contains(savedUser2.getId()));
      assertTrue(ids.contains(savedUser3.getId()));
    }

    @Test
    void shouldReturnUsersWithNullOptionalFields() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("Jane")
              .lastName("Smith")
              .email("jane.smith@example.com")
              .title(null) // Null title
              .phoneNumber(null) // Null phone number
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      // When & Then
      var result = mockMvc.perform(get(USERS_PATH)).andExpect(status().isOk()).andReturn();
      var users = userListJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(1, users.size());
      var u = users.get(0);
      assertEquals(savedUser.getId(), u.id());
      assertEquals("Jane", u.firstName());
      assertEquals("Smith", u.lastName());
      assertEquals("jane.smith@example.com", u.email());
      assertEquals("Jane Smith", u.fullName());
      assertNull(u.title());
      assertNull(u.phoneNumber());
    }

    @Test
    void shouldReturnUsersWithAllFieldsPopulated() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("Michael")
              .lastName("Johnson")
              .email("michael.johnson@example.com")
              .title("Senior Software Engineer")
              .phoneNumber("+1-555-111-2222")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      // When & Then
      var result = mockMvc.perform(get(USERS_PATH)).andExpect(status().isOk()).andReturn();
      var users = userListJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(1, users.size());
      var u = users.get(0);
      assertEquals(savedUser.getId(), u.id());
      assertEquals("Michael", u.firstName());
      assertEquals("Johnson", u.lastName());
      assertEquals("michael.johnson@example.com", u.email());
      assertEquals("Michael Johnson", u.fullName());
      assertEquals("Senior Software Engineer", u.title());
      assertEquals("+1-555-111-2222", u.phoneNumber());
    }
  }

  @Nested
  class GetUserByIdTest {

    @Test
    void shouldReturnUserWhenValidIdProvided() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      // When & Then
      var result =
          mockMvc
              .perform(get(String.format(USERS_ID_PATH, savedUser.getId())))
              .andExpect(status().isOk())
              .andReturn();
      var u = userJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(savedUser.getId(), u.id());
      assertEquals("John", u.firstName());
      assertEquals("Doe", u.lastName());
      assertEquals("john.doe@example.com", u.email());
      assertEquals("John Doe", u.fullName());
      assertEquals("Software Engineer", u.title());
      assertEquals("+1-555-123-4567", u.phoneNumber());
    }

    @Test
    void shouldReturnUserWithNullOptionalFields() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("Jane")
              .lastName("Smith")
              .email("jane.smith@example.com")
              .title(null) // Null title
              .phoneNumber(null) // Null phone number
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      // When & Then
      var result =
          mockMvc
              .perform(get(String.format(USERS_ID_PATH, savedUser.getId())))
              .andExpect(status().isOk())
              .andReturn();
      var u = userJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(savedUser.getId(), u.id());
      assertEquals("Jane", u.firstName());
      assertEquals("Smith", u.lastName());
      assertEquals("jane.smith@example.com", u.email());
      assertEquals("Jane Smith", u.fullName());
      assertNull(u.title());
      assertNull(u.phoneNumber());
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
      // Given
      var nonExistentId = UUID.randomUUID();

      // When & Then
      var result =
          mockMvc
              .perform(get(String.format(USERS_ID_PATH, nonExistentId)))
              .andExpect(status().isNotFound())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
      assertEquals("User not found with id: " + nonExistentId, problem.getDetail());
    }

    @Test
    void shouldReturn400WhenIdIsInvalidUuid() throws Exception {
      // Given
      var invalidId = "invalid-uuid";

      // When & Then
      mockMvc
          .perform(get(String.format(USERS_ID_PATH, invalidId)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenIdIsEmpty() throws Exception {
      // When & Then
      mockMvc
          .perform(get(String.format(USERS_ID_PATH, "")))
          .andExpect(status().isNotFound()); // Spring returns 404 for empty path variable
    }

    @Test
    void shouldReturn400WhenIdIsInvalidFormat() throws Exception {
      // When & Then - Test with various invalid UUID formats
      mockMvc.perform(get("/v1/users/not-a-uuid")).andExpect(status().isBadRequest());
    }
  }

  @Nested
  class PatchUserTest {

    @Test
    void shouldUpdateUserFirstNameSuccessfully() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      var patchJson =
          """
          [
            {
              "op": "replace",
              "path": "/firstName",
              "value": "Jane"
            }
          ]
          """;

      // When & Then
      var result =
          mockMvc
              .perform(
                  patch(String.format(USERS_ID_PATH, savedUser.getId()))
                      .contentType("application/json-patch+json")
                      .content(patchJson))
              .andExpect(status().isOk())
              .andReturn();
      var u = userJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(savedUser.getId(), u.id());
      assertEquals("Jane", u.firstName());
      assertEquals("Doe", u.lastName());
      assertEquals("john.doe@example.com", u.email());
      assertEquals("Jane Doe", u.fullName());
      assertEquals("Software Engineer", u.title());
      assertEquals("+1-555-123-4567", u.phoneNumber());

      // Verify database was updated
      var updatedUser = userRepository.findById(savedUser.getId());
      assertTrue(updatedUser.isPresent());
      assertEquals("Jane", updatedUser.get().getFirstName());
    }

    @Test
    void shouldUpdateUserLastNameSuccessfully() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      var patchJson =
          """
          [
            {
              "op": "replace",
              "path": "/lastName",
              "value": "Smith"
            }
          ]
          """;

      // When & Then
      var result =
          mockMvc
              .perform(
                  patch(String.format(USERS_ID_PATH, savedUser.getId()))
                      .contentType("application/json-patch+json")
                      .content(patchJson))
              .andExpect(status().isOk())
              .andReturn();
      var u = userJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(savedUser.getId(), u.id());
      assertEquals("John", u.firstName());
      assertEquals("Smith", u.lastName());
      assertEquals("john.doe@example.com", u.email());
      assertEquals("John Smith", u.fullName());
      assertEquals("Software Engineer", u.title());
    }

    @Test
    void shouldUpdateUserEmailSuccessfully() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      var patchJson =
          """
          [
            {
              "op": "replace",
              "path": "/email",
              "value": "john.smith@example.com"
            }
          ]
          """;

      // When & Then
      var result =
          mockMvc
              .perform(
                  patch(String.format(USERS_ID_PATH, savedUser.getId()))
                      .contentType("application/json-patch+json")
                      .content(patchJson))
              .andExpect(status().isOk())
              .andReturn();
      var u = userJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(savedUser.getId(), u.id());
      assertEquals("John", u.firstName());
      assertEquals("Doe", u.lastName());
      assertEquals("john.smith@example.com", u.email());
      assertEquals("John Doe", u.fullName());
      assertEquals("Software Engineer", u.title());
    }

    @Test
    void shouldUpdateUserTitleSuccessfully() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      var patchJson =
          """
          [
            {
              "op": "replace",
              "path": "/title",
              "value": "Senior Software Engineer"
            }
          ]
          """;

      // When & Then
      var result =
          mockMvc
              .perform(
                  patch(String.format(USERS_ID_PATH, savedUser.getId()))
                      .contentType("application/json-patch+json")
                      .content(patchJson))
              .andExpect(status().isOk())
              .andReturn();
      var u = userJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(savedUser.getId(), u.id());
      assertEquals("John", u.firstName());
      assertEquals("Doe", u.lastName());
      assertEquals("john.doe@example.com", u.email());
      assertEquals("John Doe", u.fullName());
      assertEquals("Senior Software Engineer", u.title());
    }

    @Test
    void shouldUpdateUserPhoneNumberSuccessfully() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      var patchJson =
          """
          [
            {
              "op": "replace",
              "path": "/phoneNumber",
              "value": "+1-555-987-6543"
            }
          ]
          """;

      // When & Then
      var result =
          mockMvc
              .perform(
                  patch(String.format(USERS_ID_PATH, savedUser.getId()))
                      .contentType("application/json-patch+json")
                      .content(patchJson))
              .andExpect(status().isOk())
              .andReturn();
      var u = userJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(savedUser.getId(), u.id());
      assertEquals("John", u.firstName());
      assertEquals("Doe", u.lastName());
      assertEquals("john.doe@example.com", u.email());
      assertEquals("John Doe", u.fullName());
      assertEquals("Software Engineer", u.title());
      assertEquals("+1-555-987-6543", u.phoneNumber());
    }

    @Test
    void shouldUpdateMultipleFieldsSuccessfully() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      var patchJson =
          """
          [
            {
              "op": "replace",
              "path": "/firstName",
              "value": "Jane"
            },
            {
              "op": "replace",
              "path": "/lastName",
              "value": "Smith"
            },
            {
              "op": "replace",
              "path": "/title",
              "value": "Senior Software Engineer"
            }
          ]
          """;

      // When & Then
      var result =
          mockMvc
              .perform(
                  patch(String.format(USERS_ID_PATH, savedUser.getId()))
                      .contentType("application/json-patch+json")
                      .content(patchJson))
              .andExpect(status().isOk())
              .andReturn();
      var u = userJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(savedUser.getId(), u.id());
      assertEquals("Jane", u.firstName());
      assertEquals("Smith", u.lastName());
      assertEquals("john.doe@example.com", u.email());
      assertEquals("Jane Smith", u.fullName());
      assertEquals("Senior Software Engineer", u.title());
      assertEquals("+1-555-123-4567", u.phoneNumber());
    }

    @Test
    void shouldSetOptionalFieldToNull() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      var patchJson =
          """
          [
            {
              "op": "replace",
              "path": "/title",
              "value": null
            },
            {
              "op": "replace",
              "path": "/phoneNumber",
              "value": null
            }
          ]
          """;

      // When & Then
      var result =
          mockMvc
              .perform(
                  patch(String.format(USERS_ID_PATH, savedUser.getId()))
                      .contentType("application/json-patch+json")
                      .content(patchJson))
              .andExpect(status().isOk())
              .andReturn();
      var u = userJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(savedUser.getId(), u.id());
      assertEquals("John", u.firstName());
      assertEquals("Doe", u.lastName());
      assertEquals("john.doe@example.com", u.email());
      assertEquals("John Doe", u.fullName());
      assertNull(u.title());
      assertNull(u.phoneNumber());

      // Verify database was updated
      var updatedUser = userRepository.findById(savedUser.getId());
      assertTrue(updatedUser.isPresent());
      assertNull(updatedUser.get().getTitle());
      assertNull(updatedUser.get().getPhoneNumber());
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
      // Given
      var nonExistentId = UUID.randomUUID();
      var patchJson =
          """
          [
            {
              "op": "replace",
              "path": "/firstName",
              "value": "Jane"
            }
          ]
          """;

      // When & Then
      var result =
          mockMvc
              .perform(
                  patch(String.format(USERS_ID_PATH, nonExistentId))
                      .contentType("application/json-patch+json")
                      .content(patchJson))
              .andExpect(status().isNotFound())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
      assertEquals("User not found with id: " + nonExistentId, problem.getDetail());
    }

    @Test
    void shouldReturn400WhenPatchJsonIsMalformed() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      var malformedPatchJson =
          """
          [
            {
              "op": "replace",
              "path": "/firstName",
              "value": "Jane"
          ]
          """;

      // When & Then
      mockMvc
          .perform(
              patch(String.format(USERS_ID_PATH, savedUser.getId()))
                  .contentType("application/json-patch+json")
                  .content(malformedPatchJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPatchOperationIsInvalid() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      var invalidPatchJson =
          """
          [
            {
              "op": "invalid_operation",
              "path": "/firstName",
              "value": "Jane"
            }
          ]
          """;

      // When & Then
      mockMvc
          .perform(
              patch(String.format(USERS_ID_PATH, savedUser.getId()))
                  .contentType("application/json-patch+json")
                  .content(invalidPatchJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTryingToPatchInvalidField() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      var patchJson =
          """
          [
            {
              "op": "replace",
              "path": "/invalidField",
              "value": "someValue"
            }
          ]
          """;

      // When & Then
      mockMvc
          .perform(
              patch(String.format(USERS_ID_PATH, savedUser.getId()))
                  .contentType("application/json-patch+json")
                  .content(patchJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenIdIsInvalidUuid() throws Exception {
      // Given
      var invalidId = "invalid-uuid";
      var patchJson =
          """
          [
            {
              "op": "replace",
              "path": "/firstName",
              "value": "Jane"
            }
          ]
          """;

      // When & Then
      mockMvc
          .perform(
              patch(String.format(USERS_ID_PATH, invalidId))
                  .contentType("application/json-patch+json")
                  .content(patchJson))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class DeleteUserTest {

    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .title("Software Engineer")
              .phoneNumber("+1-555-123-4567")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      // Verify user exists before deletion
      assertTrue(userRepository.existsById(savedUser.getId()));

      // When & Then
      mockMvc
          .perform(delete(String.format(USERS_ID_PATH, savedUser.getId())))
          .andExpect(status().isNoContent());

      // Verify user was deleted from database
      assertFalse(userRepository.existsById(savedUser.getId()));
      var deletedUser = userRepository.findById(savedUser.getId());
      assertTrue(deletedUser.isEmpty());
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
      // Given
      var nonExistentId = UUID.randomUUID();

      // When & Then
      var result =
          mockMvc
              .perform(delete(String.format(USERS_ID_PATH, nonExistentId)))
              .andExpect(status().isNotFound())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
      assertEquals("User not found with id: " + nonExistentId, problem.getDetail());
    }

    @Test
    void shouldReturn400WhenIdIsInvalidUuid() throws Exception {
      // Given
      var invalidId = "invalid-uuid";

      // When & Then
      mockMvc
          .perform(delete(String.format(USERS_ID_PATH, invalidId)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenIdIsEmpty() throws Exception {
      // When & Then
      mockMvc
          .perform(delete(String.format(USERS_ID_PATH, "")))
          .andExpect(status().isNotFound()); // Spring returns 404 for empty path variable
    }

    @Test
    void shouldReturn400WhenIdIsInvalidFormat() throws Exception {
      // When & Then - Test with various invalid UUID formats
      mockMvc.perform(delete("/v1/users/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenTryingToDeleteSameUserTwice() throws Exception {
      // Given
      var userEntity =
          UserEntity.builder()
              .firstName("John")
              .lastName("Doe")
              .email("john.doe@example.com")
              .createdBy(UUID.randomUUID())
              .lastModifiedBy(UUID.randomUUID())
              .build();
      var savedUser = userRepository.save(userEntity);

      // When - Delete user first time (should succeed)
      mockMvc
          .perform(delete(String.format(USERS_ID_PATH, savedUser.getId())))
          .andExpect(status().isNoContent());

      // Verify user was deleted
      assertFalse(userRepository.existsById(savedUser.getId()));

      // Then - Try to delete same user again (should return 404)
      var result =
          mockMvc
              .perform(delete(String.format(USERS_ID_PATH, savedUser.getId())))
              .andExpect(status().isNotFound())
              .andReturn();
      var problem = problemDetailJacksonTester.parseObject(result.getResponse().getContentAsString());
      assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
      assertEquals("User not found with id: " + savedUser.getId(), problem.getDetail());
    }
  }

  private static final String USERS_PATH = "/v1/users";
  private static final String USERS_ID_PATH = "/v1/users/%s";
}
