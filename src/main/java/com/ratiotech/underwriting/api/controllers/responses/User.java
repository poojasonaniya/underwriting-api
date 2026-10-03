package com.ratiotech.underwriting.api.controllers.responses;

import java.util.UUID;

public record User(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String fullName,
    String title,
    String phoneNumber) {}
