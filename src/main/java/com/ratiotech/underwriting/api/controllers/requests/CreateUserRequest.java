package com.ratiotech.underwriting.api.controllers.requests;

import jakarta.validation.constraints.NotEmpty;

public record CreateUserRequest(
    @NotEmpty(message = "'firstName' is required") String firstName,
    @NotEmpty(message = "'lastName' is required") String lastName,
    @NotEmpty(message = "'email' is required") String email,
    String title,
    String phoneNumber) {}
