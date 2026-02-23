package com.springbootdeveloper.DTO;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ContactDto {

    private UUID contactId;

    @NotBlank
    @Size(min = 6, message = "Name must be at least 6 characters long")
    @Pattern(
        regexp = "^[A-Za-z]+( [A-Za-z]+)*$",
        message = "Name can contain only letters and spaces (no leading or trailing spaces)"
    )
    private String name;

    @NotBlank
    @Size(min = 6, message = "Nick name must be at least 6 characters long")
    @Pattern(
        regexp = "^[A-Za-z0-9]+( [A-Za-z0-9]+)*$",
        message = "Nick name can contain only letters, digits, and spaces (no leading or trailing spaces)"
    )
    private String nickName;

    private String work;

    private String description;

    private long phone;

    @NotBlank
    @Email(message = "Not a valid email")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
        message = "Email must end with @gmail.com"
    )
    private String email;

    private String imageURL;

    // ===== getters & setters =====

    public UUID getContactId() {
        return contactId;
    }

    public void setContactId(UUID contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
