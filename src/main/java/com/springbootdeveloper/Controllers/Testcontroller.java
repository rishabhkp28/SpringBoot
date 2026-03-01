package com.springbootdeveloper.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;


import com.springbootdeveloper.DTO.ProfileEnhanceDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.Exceptions.EmptyFileException;
import com.springbootdeveloper.Exceptions.FileSizeException;
import com.springbootdeveloper.Exceptions.FileStorageException;
import com.springbootdeveloper.Exceptions.UnsupportedFileTypeException;
import com.springbootdeveloper.Exceptions.UserNotFoundException;
import com.springbootdeveloper.ServiceLayer.ServiceClass;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class Testcontroller {
	
	@Autowired
	private ServiceClass sc;
	
	
	
	
	@GetMapping(path = "/getStarted")
	public String launchPage()
	{	
		
		System.out.println("This controller is executed");
	
		return "getStarted";
	}

	@GetMapping(path = "/signUp")
	public String signUpPage(Model model)
	{
		System.out.println("SignUp page is launched");
		model.addAttribute("user",new UserDto());
		return "signUp";
	}
	
	
	@GetMapping(path = "/login") // this thing is called by Spring Security Internally ".loginPage("/login")" 
	public String loginPage(Model model)
	{
		model.addAttribute("Userlog",new UserDto());
		System.out.println("Login Page is launched");
		return "login";
	}
	
	
	
	@GetMapping(path = "/dynamic/validate/email/{email}")
    @ResponseBody
    public boolean validEmailDynamicValidation(@PathVariable("email") String email)
    {
    	
    	if(!sc.checkExisting(email))
    		return true;
    	else
    		 return false;
    	
    }
	
	@GetMapping(path = "/enhanceProfileRequest")
	public String showEnhanceProfile(Authentication authentication, Model model) {
	    
	    if (authentication == null || !authentication.isAuthenticated()) { //for now let it be here
	    	System.out.println("ReachedAuthenticationError--------------path = \"/enhanceProfileRequest\"--------------------------%%%%%@%%%$^@#");
	        return "redirect:/login";
	    }
	    
	    
	    UserDto user = sc.findByEmail(authentication.getName());
	    
	    model.addAttribute("registeredUserDto", user);
	    model.addAttribute("enhancedUser", new ProfileEnhanceDto());
	    
	    return "enhanceProfile";
	}
	
	
	
}
