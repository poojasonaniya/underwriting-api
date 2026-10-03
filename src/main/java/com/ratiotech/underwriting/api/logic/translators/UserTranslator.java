package com.ratiotech.underwriting.api.logic.translators;

import com.ratiotech.underwriting.api.controllers.requests.UpdateUserModel;
import com.ratiotech.underwriting.api.controllers.responses.User;
import com.ratiotech.underwriting.api.entities.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserTranslator {

  /**
   * Converts a UserEntity to a User response model.
   *
   * @param userEntity the UserEntity to convert
   * @return the User response model
   */
  public User toModel(UserEntity userEntity) {
    return new User(
        userEntity.getId(),
        userEntity.getEmail(),
        userEntity.getFirstName(),
        userEntity.getLastName(),
        userEntity.getFullName(),
        userEntity.getTitle(),
        userEntity.getPhoneNumber());
  }

  /**
   * Converts a UserEntity to an UpdateUserModel.
   *
   * @param userEntity the UserEntity to convert
   * @return the UpdateUserModel
   */
  public UpdateUserModel toUpdateModel(UserEntity userEntity) {
    return new UpdateUserModel(
        userEntity.getFirstName(),
        userEntity.getLastName(),
        userEntity.getEmail(),
        userEntity.getTitle(),
        userEntity.getPhoneNumber());
  }

  /**
   * Updates a UserEntity with values from an UpdateUserModel.
   *
   * @param userEntity the UserEntity to update
   * @param updateModel the UpdateUserModel containing new values
   */
  public void updateEntity(UserEntity userEntity, UpdateUserModel updateModel) {
    userEntity.setFirstName(updateModel.firstName());
    userEntity.setLastName(updateModel.lastName());
    userEntity.setEmail(updateModel.email());
    userEntity.setTitle(updateModel.title());
    userEntity.setPhoneNumber(updateModel.phoneNumber());
  }
}
