package com.springbootdeveloper.DTO;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Size;

public class ProfileEnhanceDto {
	
	
	
	@Size(max = 200, message = "Bio cannot exceed 200 characters")
	private String bio;
	
	
	private MultipartFile multipartFile;


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
	
	

}
