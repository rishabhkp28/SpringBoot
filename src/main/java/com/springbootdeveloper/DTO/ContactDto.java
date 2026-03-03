package com.springbootdeveloper.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.springbootdeveloper.Models.ContactGroup;

public class ContactDto {

    private UUID contactId;

    @NotBlank
    @Size(min = 6, message = "Name must be at least 6 characters long")
    @Pattern(regexp = "^[A-Za-z]+( [A-Za-z]+)*$", message = "Name can contain only letters and spaces")
    private String name;

    @Size(min = 6, message = "Nick name must be at least 6 characters long")
    @Pattern(regexp = "^[A-Za-z0-9]+( [A-Za-z0-9]+)*$", message = "Nick name can contain only letters, digits, and spaces")
    private String nickName;

    
    private String description;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @Email(message = "Not a valid email")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", 
    message = "Email must end with @gmail.com",
    flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email;

    private MultipartFile multipartFile;
    private String fileName;

    // ===== New fields =====
    private LocalDateTime createdDate;
    
    
    @NotNull(message = "Contact group is required")
    private ContactGroup group;
    
    private LocalDateTime updatedDate;  

    // ===== getters & setters =====
    public UUID getContactId() { return contactId; }
    public void setContactId(UUID contactId) { this.contactId = contactId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

   

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public MultipartFile getMultipartFile() { return multipartFile; }
    public void setMultipartFile(MultipartFile image) { this.multipartFile = multipartFile; }
    
    
    public String getFileName() {
    	return fileName;
    }
    public void setFileName(String fileName) {
    	this.fileName = fileName;
    }
    
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }//needed for  conversion to DTO

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; } //needed for  conversion to DTO


    public ContactGroup getGroup() { return group; }
    public void setGroup(ContactGroup group) { this.group = group; }
    
    
    
    
    
}