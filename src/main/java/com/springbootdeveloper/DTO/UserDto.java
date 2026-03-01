package com.springbootdeveloper.DTO;

import jakarta.validation.constraints.*;
import java.util.*;


public class UserDto {

    private UUID userId;

    @NotBlank
    @Size(min = 3, message = "Name must be at least 3 characters long")
    @Pattern(
        regexp = "^[A-Za-z]+( [A-Za-z]+)*$",
        message = "Name can contain only letters and spaces"
    )
    private String name;

    @NotBlank
    @Email(message = "Not a valid email")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Email must end with @gmail.com"
    )
    private String email;

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
        message = "Password must be strong"
    )
    private String password;

    private String image;

    private String bio;

    private boolean enable;

    private String role;

    @AssertTrue(message = "You must agree to the terms")
    private boolean agreeTerms;

    private Set<ContactDto> contactDtos = new HashSet<>();

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isAgreeTerms() {
		return agreeTerms;
	}

	public void setAgreeTerms(boolean agreeTerms) {
		this.agreeTerms = agreeTerms;
	}

	public Set<ContactDto> getContactDtos() {
		return contactDtos;
	}

	public void setContactDtos(Set<ContactDto> contactDtos) {
		this.contactDtos = contactDtos;
	}

    
}
