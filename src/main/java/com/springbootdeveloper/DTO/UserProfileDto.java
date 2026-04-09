package com.springbootdeveloper.DTO;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserProfileDto {
		
		@NotBlank
	    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
	    @Pattern(
	        regexp = "^[A-Za-z]+( [A-Za-z]+)*$",
	        message = "Name can contain only letters and spaces"
	    )
	    private String name;
		private String fileName;
	    @NotBlank
	    @Email(message = "Not a valid email")
	    @Size(max = 300, message = "Email must not exceed 300 characters")
	    @Pattern(
	        regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
	        flags = Pattern.Flag.CASE_INSENSITIVE,
	        message = "Email must end with @gmail.com"
	    )
	    private String email;
	    
	   
	    private String currentPassword;
	    
	    private String image;
	    private MultipartFile multipartFile; //custom validation
	    private boolean removePhoto;
		
		@Size(max = 200, message = "Bio cannot exceed 200 characters")
		private String bio;
		
		private UUID userId;
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
		public String getCurrentPassword() {
			return currentPassword;
		}
		public void setCurrentPassword(String currentPassword) {
			this.currentPassword = currentPassword;
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
		
		public MultipartFile getMultipartFile() {
			return multipartFile;
		}
		public void setMultipartFile(MultipartFile multipartFile) {
			this.multipartFile = multipartFile;
		}
		public boolean isRemovePhoto() {
			return removePhoto;
		}
		public void setRemovePhoto(boolean removePhoto) {
			this.removePhoto = removePhoto;
		}
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		
		
}
