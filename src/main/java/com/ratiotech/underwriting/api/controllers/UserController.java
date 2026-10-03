package com.ratiotech.underwriting.api.controllers;

import com.github.fge.jsonpatch.JsonPatch;
import com.ratiotech.underwriting.api.controllers.requests.CreateUserRequest;
import com.ratiotech.underwriting.api.controllers.responses.User;
import com.ratiotech.underwriting.api.logic.UserLogic;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

  private final UserLogic userLogic;

  public UserController(UserLogic userLogic) {
    this.userLogic = userLogic;
  }

  @GetMapping(path = "/v1/users")
  public ResponseEntity<List<User>> getAllUsers() {
    var users = userLogic.getAllUsers();
    return ResponseEntity.ok(users);
  }

  @PostMapping(path = "/v1/users")
  public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
    var userId = userLogic.createUser(request);

    var location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(userId)
            .toUri();

    return ResponseEntity.created(location).build();
  }

  @GetMapping(path = "/v1/users/{id}")
  public ResponseEntity<User> getUserById(@PathVariable UUID id) {
    var user = userLogic.getUserById(id);
    return ResponseEntity.ok(user);
  }

  @PatchMapping(path = "/v1/users/{id}")
  public ResponseEntity<User> patchUser(@PathVariable UUID id, @RequestBody JsonPatch patch) {
    var updatedUser = userLogic.patchUser(id, patch);
    return ResponseEntity.ok(updatedUser);
  }

  @DeleteMapping(path = "/v1/users/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
    userLogic.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
