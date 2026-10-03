package com.ratiotech.underwriting.api.logic;

import com.github.fge.jsonpatch.JsonPatch;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserLogic {

  private final UserRepository userRepository;
  private final UserTranslator userTranslator;
  private final JsonPatchManager jsonPatchManager;

  @Autowired
  public UserLogic(
      UserRepository userRepository,
      UserTranslator userTranslator,
      JsonPatchManager jsonPatchManager) {
    this.userRepository = userRepository;
    this.userTranslator = userTranslator;
    this.jsonPatchManager = jsonPatchManager;
  }

  /**
   * Creates a new user from the provided request.
   *
   * @param request the create user request containing user details
   * @return the UUID of the created user
   * @throws BadRequestException if a user with the same email already exists
   */
  @Transactional
  public UUID createUser(CreateUserRequest request) {
    // Check if user already exists by email
    if (userRepository.existsByEmail(request.email())) {
      throw new BadRequestException("User with email '" + request.email() + "' already exists");
    }

    // Create new user entity
    var userEntity =
        UserEntity.builder()
            .email(request.email())
            .firstName(request.firstName())
            .lastName(request.lastName())
            .title(request.title())
            .phoneNumber(request.phoneNumber())
            .createdBy(Constants.INSTANCE.getSYSTEM_IDENTITY())
            .lastModifiedBy(Constants.INSTANCE.getSYSTEM_IDENTITY())
            .build();

    // Save the user and return its UUID
    var savedUser = userRepository.save(userEntity);
    return savedUser.getId();
  }

  /**
   * Retrieves a user by their ID.
   *
   * @param userId the UUID of the user to retrieve
   * @return the User response model
   * @throws NotFoundException if no user is found with the given ID
   */
  @Transactional(readOnly = true)
  public User getUserById(UUID userId) {
    var userEntity =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    return userTranslator.toModel(userEntity);
  }

  /**
   * Retrieves all users.
   *
   * @return a list of all User response models
   */
  @Transactional(readOnly = true)
  public List<User> getAllUsers() {
    return userRepository.findAll().stream()
        .map(userTranslator::toModel)
        .collect(Collectors.toList());
  }

  /**
   * Patches a user using JSON Patch operations.
   *
   * @param userId the UUID of the user to patch
   * @param patch the JSON patch operations to apply
   * @return the updated User response model
   * @throws NotFoundException if no user is found with the given ID
   */
  @Transactional
  public User patchUser(UUID userId, JsonPatch patch) {
    // 1. Find the user entity
    var userEntity =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

    // 2. Convert entity to update model
    var updateModel = userTranslator.toUpdateModel(userEntity);

    // 3. Apply JSON patch to the update model
    var patchedUpdateModel = jsonPatchManager.applyPatch(patch, updateModel, UpdateUserModel.class);

    // 4. Update the entity with the patched values
    userTranslator.updateEntity(userEntity, patchedUpdateModel);

    // 5. Save the updated entity
    var savedEntity = userRepository.save(userEntity);

    // 6. Return the updated user as response model
    return userTranslator.toModel(savedEntity);
  }

  /**
   * Deletes a user by their ID.
   *
   * @param userId the UUID of the user to delete
   * @throws NotFoundException if no user is found with the given ID
   */
  public void deleteUser(UUID userId) {
    // Check if user exists before attempting to delete
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("User not found with id: " + userId);
    }
    userRepository.deleteById(userId);
  }
}
