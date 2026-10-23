package com.djiguiya.transport.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload for an authenticated password change (current + new password). */
public record ChangePasswordRequest(
		@NotBlank String currentPassword,
		@NotBlank @Size(min = 8, max = 100) String newPassword
) {
}
