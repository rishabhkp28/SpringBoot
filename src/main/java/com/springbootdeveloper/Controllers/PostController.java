package com.springbootdeveloper.Controllers;
// helps the user to connect to us

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

import org.springframework.web.bind.annotation.PostMapping;
import com.springbootdeveloper.DTO.ProfileEnhanceDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.ServiceLayer.ServiceClassContact;
import com.springbootdeveloper.ServiceLayer.ServiceClassUser;
import com.springbootdeveloper.Exceptions.DuplicateEmailException;

import com.springbootdeveloper.Exceptions.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class PostController {

    
    private final ServiceClassUser sc;
    private ServiceClassContact serviceClassContact;

    private PostController(ServiceClassUser sc, ServiceClassContact serviceClassContact)
    {
    	this.sc = sc;
    	this.serviceClassContact = serviceClassContact;
    	
    	
    }
    /*this name inside modelAttribute matters in the thymeleaf but not in spring This name must match the th:object name in the page ,during addattribute()
      for proper binding although modelattribute annotation can be used without th:object ...but for displaying binding result we need to use th:objects
     */
    @PostMapping(path = "/signUp") 
    public String handleSubmit(
            @Valid @ModelAttribute("user") UserDto userDto,
            BindingResult result, HttpServletRequest request, HttpServletResponse response
           
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
       
        sc.autoLogin(userDto, response);//sent by the user
        
        return "redirect:/enhanceProfileRequest";
    }
    
    
    @PostMapping(path = "/enhance-form")
    public String handleEnhancedForm(@Valid @ModelAttribute("enhancedUser") ProfileEnhanceDto enhancedProfile,BindingResult result,Authentication authentication, Model model)
    {    	
    	
    	String fileName= "";
    	UserDto userDto = sc.findByEmail(authentication.getName());
    	
    	if(result.hasErrors()) {
    		model.addAttribute("registeredUserDto",userDto);
    		//no need for second as spring remembers that profileEnhanced and its binded already
    		return "enhanceProfile";
    	}
    	
    	try
    	{
    		userDto = sc.save(userDto,enhancedProfile);
    	}
    	catch(RuntimeException e) {
    		
    		model.addAttribute("registeredUserDto", userDto);
    	    result.rejectValue("multipartFile", e.getMessage());
    	    return "enhanceProfile";
    	}
    			    	
	    	model.addAttribute("user",userDto);
	 		model.addAttribute("activePage","dashboard");
	 		model.addAttribute("pageTitle","Dashboard");	
	 		model.addAttribute("pageSubtitle","Hey "+userDto.getName()+"!!! Welcome to your User Dashboard");
	 		model.addAttribute("recentContacts",null);
	 		model.addAttribute("favouritesCount",0);
	 		model.addAttribute("contactsAddedThisMonth",0);
	 		model.addAttribute("totalContactsCount",0);
	 		return "redirect:/normalUser/userDashboard";
    	
    }
}
    

