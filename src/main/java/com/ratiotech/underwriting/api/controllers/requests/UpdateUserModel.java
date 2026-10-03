package com.ratiotech.underwriting.api.controllers.requests;

public record UpdateUserModel(
    String firstName, String lastName, String email, String title, String phoneNumber) {}
