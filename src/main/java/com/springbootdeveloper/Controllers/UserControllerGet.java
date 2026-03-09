package com.springbootdeveloper.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.springbootdeveloper.ServiceLayer.ServiceClassContact;
import com.springbootdeveloper.ServiceLayer.ServiceClassUser;
import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.Exceptions.UserNotFoundException;

@Controller
public class UserControllerGet {
	
	
	private ServiceClassUser serviceClassUser;
	private ServiceClassContact serviceClassContact;
	
	public UserControllerGet(ServiceClassUser serviceClassUser,ServiceClassContact serviceClassContact)
	{
		this.serviceClassUser = serviceClassUser;
		this.serviceClassContact = serviceClassContact;
	}

	/* ----------- HELPER METHOD (ONLY ADDITION) ----------- */

	private void setPageModel(Model model,String activePage,String pageTitle,String pageSubtitle, UserDto userDto)
	{
		model.addAttribute("activePage",activePage);
		model.addAttribute("pageTitle",pageTitle);
		model.addAttribute("pageSubtitle",pageSubtitle);
		model.addAttribute("userDto",userDto);
	}
	
	@GetMapping(path ="/user/dashboard")
	public String displayDashboard(Authentication authentication,Model model)
	{
		
		UserDto userDto = null;
		long favourites = 0;
				
		try
		{
			userDto = serviceClassUser.findByEmail(authentication.getName());
			favourites = serviceClassUser.getFavouritesCount(userDto);
		}
		catch(UserNotFoundException e)
		{
			return "redirect:/logout";
		}

		

		setPageModel(
				model,
				"dashboard",
				"Dashboard",
				"Hey "+userDto.getName()+"!!! Welcome to your User Dashboard",userDto
		);
		
		model.addAttribute("favouritesCount",serviceClassUser.getFavouritesCount(userDto));
		model.addAttribute("recentContacts",serviceClassContact.findLast10(userDto.getUserId()));
		return "normalUser/userDashboard";
	}

	@GetMapping(path ="/user/contacts")
	public String displayContacts(Authentication authentication,Model model)
	{
		UserDto userDto = null;
				
		try
		{
			userDto = serviceClassUser.findByEmail(authentication.getName());
		}
		catch(UserNotFoundException e)
		{
			return "redirect:/logout";
		}

	

		setPageModel(
				model,
				"contacts",
				"Contacts",
				"Hey "+userDto.getName()+"!!! Below shows your list of contacts saved on our cloud",userDto
		);

		return "normalUser/userContacts";
	}

	@GetMapping(path ="/user/favourites")
	public String displayFavourites(Authentication authentication,Model model)
	{
		UserDto userDto = null;
				
		try
		{
			userDto = serviceClassUser.findByEmail(authentication.getName());
		}
		catch(UserNotFoundException e)
		{
			return "redirect:/logout";
		}

		

		setPageModel(
				model,
				"favourites",
				"Favourites",
				"Hey "+userDto.getName()+"!!! Below shows your favourite contacts saved on our cloud",userDto
		);

		return "normalUser/userFavourites";
	}
	
	@GetMapping(path ="/user/groups")
	public String displayGroups(Authentication authentication,Model model)
	{
		UserDto userDto = null;

		try
		{
			userDto = serviceClassUser.findByEmail(authentication.getName());
		}
		catch(UserNotFoundException e)
		{
			return "redirect:/logout";
		}

		

		setPageModel(
				model,
				"groups",
				"Groups",
				"Hey "+userDto.getName()+"!!! Below shows your customized Groups", userDto
		);

		return "normalUser/userGroups";
	}

	@GetMapping(path ="/user/import")
	public String displayImport(Authentication authentication,Model model)
	{
		
		UserDto userDto = null;
				
		try
		{
			userDto = serviceClassUser.findByEmail(authentication.getName());
		}
		catch(UserNotFoundException e)
		{
			return "redirect:/logout";
		}
    	
		

		setPageModel(
				model,
				"import",
				"Import",
				"Hey "+userDto.getName()+"!!! Below are the import functionalities available",userDto
		);

		return "normalUser/userImport";
	}

	@GetMapping(path ="/user/profile")
	public String displaProfile(Authentication authentication,Model model)
	{
		UserDto userDto = null;
				
		try
		{
			userDto = serviceClassUser.findByEmail(authentication.getName());
		}
		catch(UserNotFoundException e)
		{
			return "redirect:/logout";
		}
		    	
		

		setPageModel(
				model,
				"profile",
				"Profile",
				"Hey "+userDto.getName()+"!!! Below is your Profile on our server",userDto
		);

		return "normalUser/userProfile";
	}

	@GetMapping(path ="/user/settings")
	public String displaySettings(Authentication authentication,Model model)
	{
		
		UserDto userDto = null;
		
		try
		{
			userDto = serviceClassUser.findByEmail(authentication.getName());
		}
		catch(UserNotFoundException e)
		{
			return "redirect:/logout";
		}

	

		setPageModel(
				model,
				"settings",
				"Settings",
				"Hey "+userDto.getName()+"!!!Configure as you want",userDto
		);

		return "normalUser/userSettings";
	}
	
	@GetMapping(path ="/user/addContactRequest")
	public String handleAddContact(Authentication authentication,Model model)
	{
		UserDto userDto = null;
		
		try
		{
			userDto = serviceClassUser.findByEmail(authentication.getName());
		}
		catch(UserNotFoundException e)
		{
			return "redirect:/logout";
		}
		
		

		setPageModel(
				model,
				"dashboard",
				"Dashboard",
				"Hey "+userDto.getName()+"!!! Welcome to your User Dashboard",userDto
		);

	    model.addAttribute("contactDto", new ContactDto());

		return "normalUser/addContact";		
		
	}
	
	
	
	
	
	
	
	
}