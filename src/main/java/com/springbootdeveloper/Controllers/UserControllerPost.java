package com.springbootdeveloper.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.ServiceLayer.ServiceClassContact;

import jakarta.validation.Valid;

@Controller
public class UserControllerPost {
	
	@Autowired
	public static ServiceClassContact serviceClassContact;
	
	
	@PostMapping(path = "/user/addContactRequest")
	public String saveContact(@Valid @ModelAttribute ContactDto contactDto,BindingResult results)
	{
		
		//default  validation first
		if(results.hasErrors())
		{
			return "addContact";
		}
	
		//custom errors integrated by me
		try
		{
			serviceClassContact.saveContact(contactDto);
		}
		catch(RuntimeException e)
		{ //errors will be due to files only
			results.reject("multipartFile", e.getMessage());
			return "addContact";
		}
		
		return "successpage";
	}
	

}
