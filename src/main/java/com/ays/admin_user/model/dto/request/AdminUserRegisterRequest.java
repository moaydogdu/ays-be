package com.ays.admin_user.model.dto.request;

import com.ays.common.model.AysPhoneNumber;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a request to register a new administrator user. The request includes fields for the required user
 * information, such as the user's username, email, password, and phone number, as well as their first and last name.
 */
@Data
@Builder
public class AdminUserRegisterRequest {

    /**
     * Registration Verification ID for Registering Admin User
     */
    @NotBlank
    private String verificationId;

    @NotBlank
    private String institutionId;

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @Valid
    @NotNull
    private AysPhoneNumber phoneNumber;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

}
