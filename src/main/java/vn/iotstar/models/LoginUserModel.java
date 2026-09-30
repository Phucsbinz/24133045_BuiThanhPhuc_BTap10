package vn.iotstar.models;
import jakarta.validation.constraints.*;
public record LoginUserModel(@NotBlank @Email String email, @NotBlank String password) {}

