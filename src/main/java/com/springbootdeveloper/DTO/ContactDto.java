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
    // ✅ FIX — removed duplicate @NotBlank (was declared twice)
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    @Pattern(
        regexp = "^[A-Za-z]+( [A-Za-z]+)*$",
        message = "Name can contain only letters and single space (not trailing) between words"
    )
    private String name;
    // ✅ CORRECT — empty string OR letters/digits with single non-trailing spaces
    @Size(max = 100, message = "Nick name must not exceed 100 characters")
    @Pattern(
        regexp = "^$|^[A-Za-z0-9]+( [A-Za-z0-9]+)*$",
        message = "Nick name can contain letters, digits, and single spaces (not trailing) between words, or it can be empty"
    )
    private String nickName;
    
    
    @Size(max = 200)
    private String description;
    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^[0-9]{10}$",
        message = "Phone number must be exactly 10 digits"
    )
    private String phone;
    // ✅ CORRECT — empty string OR valid gmail address (case insensitive)
    // @Email handles basic format check; @Pattern enforces @gmail.com specifically
    @Email(message = "Not a valid email")
    @Size(max = 300, message = "Email must not exceed 300 characters")
    @Pattern(
        regexp = "^$|^[A-Za-z0-9._%+\\-]+@gmail\\.com$",
        message = "Email must end with @gmail.com",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    private String email;
    private MultipartFile multipartFile;
    private String fileName;
    private boolean favourite;
    private LocalDateTime createdDate;
    @NotNull(message = "Contact group is required")
    private ContactGroup group; // if ContactGroup can't convert to this enum, BindingResult throws error
    private LocalDateTime updatedDate;
    // ===== Getters & Setters =====
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
    public void setMultipartFile(MultipartFile multipartFile) { this.multipartFile = multipartFile; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    public ContactGroup getGroup() { return group; }
    public void setGroup(ContactGroup group) { this.group = group; }
    public boolean getFavourite() { return favourite; }
    public void setFavourite(boolean favourite) { this.favourite = favourite; }
    
    private boolean removePhoto = false;
    public boolean isRemovePhoto() { return removePhoto; }
    public void setRemovePhoto(boolean removePhoto) { this.removePhoto = removePhoto; }
}