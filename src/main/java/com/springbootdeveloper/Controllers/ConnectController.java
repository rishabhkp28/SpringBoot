package com.springbootdeveloper.Controllers;
// helps the user to connect to us


import java.security.Principal;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.web.bind.annotation.PostMapping;
import com.springbootdeveloper.DTO.ProfileEnhanceDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.ServiceLayer.ServiceClass;
import com.springbootdeveloper.Exceptions.DuplicateEmailException;
import com.springbootdeveloper.Exceptions.EmptyFileException;
import com.springbootdeveloper.Exceptions.FileSizeException;
import com.springbootdeveloper.Exceptions.FileStorageException;
import com.springbootdeveloper.Exceptions.UnsupportedFileTypeException;
import com.springbootdeveloper.Exceptions.UserNotFoundException;
import com.springbootdeveloper.Helpers.FileUploader;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class ConnectController {

    
    private final ServiceClass sc;
    private final FileUploader fileUploader;
    
    private ConnectController(ServiceClass sc,FileUploader fileUploader)
    {
    	this.sc = sc;
    	this.fileUploader = fileUploader;
    	
    }
    /*this name inside modelAttribute matters in the thymeleaf but not in spring This name must match the th:object name in the page ,during addattribute()
      for proper binding although modelattribute annotation can be used without th:object ...but for displaying binding result we need to use th:objects
     */
    @PostMapping(path = "/signUp") 
    public String handleSubmit(
            @Valid @ModelAttribute("user") UserDto userDto,
            BindingResult result, HttpServletRequest request
           
    ) // I can even skip this annotation @ModelAttribute here springboot automates it
    {

        // Step 1: standard validation only (@NotBlank, @Email, @Pattern)
        if (result.hasErrors()) {
            return "signUp";
        }
        UserDto user = null;
        // Step 2: delegate uniqueness & persistence to service
        try {
            user = sc.save(userDto);
        } catch (DuplicateEmailException e)
        {

            // Step 3: map service exception to form error
            result.rejectValue(
                "email",
                "email.duplicate",
                e.getMessage()
            );

            
            return "signUp"; //when error is there
        }
       
        sc.autoLogin(userDto,request);//sent by the user
        
        return "redirect:/enhanceProfileRequest";
    }
    
    
    @PostMapping(path = "/enhance-form")
    public String handleEnhancedForm(@Valid @ModelAttribute("enhancedUser") ProfileEnhanceDto enhancedProfile,BindingResult result,Authentication authentication, Model model)
    {    	
    	//We can even skip this check as we already have configured this in spring security configuration file
    	if (authentication == null || !authentication.isAuthenticated()) {
    		System.out.println("This is post controller");
            return "redirect:/login"; // Send to login page
        }
    	
    	
    	String fileName= "";
    	UserDto userDto = null;
    		
	    	try
	    	{
	    		int count = 0;
	    		userDto = sc.findByEmail(authentication.getName());
	    		if(enhancedProfile.getMultipartFile()!= null)
	        	{
	    			fileName = fileUploader.upload(enhancedProfile.getMultipartFile());
	    			userDto.setImage(fileName);
	    			count = 1;
	        	}
	    		
	            if (enhancedProfile.getBio() != null && 
	                !enhancedProfile.getBio().isBlank()) {
	            	userDto.setBio(enhancedProfile.getBio());
	                count +=1 ;
	            }
	            
	            if(count!=0)
	            	userDto =  sc.update(userDto);
	            
	            
	   
	    	}
	    	catch(EmptyFileException | FileSizeException | UnsupportedFileTypeException | FileStorageException |UserNotFoundException e) {
	    	    result.rejectValue("multipartFile", e.getMessage());
	    	    return "enhanceProfile";
	    	}
    	
    	
	    	 model.addAttribute("User", userDto);
	    	 return "dashboard";
    	
    }
    
    /*
    @PostMapping(path = "/login")
    public String handleLogin(@Valid @ModelAttribute("Userlog")UserDto dto,BindingResult result,HttpServletRequest request)
    {
    	//case when the user exists
    	try {
            sc.autoLogin(dto, request);
            return "redirect:/dashboard";//this is a request
        } catch (BadCredentialsException e) {
            result.reject("login.error", "Invalid email or password");
            return "login";
        }
    	
    	
    	//case when the user did not exist but tries to Log In
    	/// 
    	In this case we must not tell whether user exists or not or whether invalid credentials exist for security purposes
    	 * to handle User Enumeration Attacks
    	
    	
    	This method is not useful as the login is already handled by the spring security in the security configuration 
		*/
    	
    	
 }
    
    
    
    
    
    

