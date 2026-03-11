package com.springbootdeveloper.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.springbootdeveloper.ServiceLayer.ServiceClassContact;
import com.springbootdeveloper.ServiceLayer.ServiceClassUser;
import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.Exceptions.UserNotFoundException;
import com.springbootdeveloper.Models.ContactGroup;

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
		model.addAttribute("user",userDto);
	}
	
	@GetMapping(path ="/user/dashboard")
	public String displayDashboard(Authentication authentication,Model model)
	{
		
		UserDto userDto = null;
		long favourites = 0;
				
		try
		{
			userDto = serviceClassUser.findByEmail(authentication.getName());
			favourites = serviceClassContact.getFavouritesCount(userDto.getUserId());
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
		
		model.addAttribute("favouritesCount",serviceClassContact.getFavouritesCount(userDto.getUserId()));
		model.addAttribute("recentContacts",serviceClassContact.findLast10(userDto.getUserId()));
		model.addAttribute("totalContactsCount",serviceClassContact.getContactsCount(userDto.getUserId()));
		model.addAttribute("contactsAddedThisMonth",serviceClassContact.getThisMonthContactCount(userDto.getUserId()));
		
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
		
		
		model.addAttribute("userContacts",serviceClassContact.findAllContacts(userDto.getUserId()));
		return "normalUser/userContacts";
	}

	@GetMapping(path ="/user/favouriteContacts")
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
		model.addAttribute("userFavourites",serviceClassContact.getFavouriteContacts(userDto.getUserId()));

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
		
		model.addAttribute("friendsCount",serviceClassContact.getCountByGroup(userDto.getUserId(), ContactGroup.FRIEND));
		model.addAttribute("familyCount",serviceClassContact.getCountByGroup(userDto.getUserId(), ContactGroup.FAMILY));
		model.addAttribute("anonymousCount",serviceClassContact.getCountByGroup(userDto.getUserId(), ContactGroup.ANONYMOUS));
		model.addAttribute("clietsCount",serviceClassContact.getCountByGroup(userDto.getUserId(), ContactGroup.CLIENT));
		model.addAttribute("workCount",serviceClassContact.getCountByGroup(userDto.getUserId(), ContactGroup.WORK));
		
		
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
	
	@GetMapping(path ="/user/groups/{Group}")
	public String displayGroupedContacts(Authentication authentication,@PathVariable("Group") ContactGroup group,Model model)
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
							"Hey "+userDto.getName()+"!!!Here are the groups that exist in our system",userDto
					);

		model.addAttribute("groupContacts", serviceClassContact.getContactsByGroup(userDto.getUserId(), group));
		model.addAttribute("nameOfGroup",group.toString());
		return "normalUser/groupContacts";
		
	}
	
	@GetMapping(path ="/user/contactsForThisMonth")
	public String displayContactsForThisMonth(Authentication authentication,Model model)
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
							"Hey "+userDto.getName()+"!!!Here are the contacts that you added this month",userDto
					);

					

		model.addAttribute("contactsAddedThisMonth", serviceClassContact.getContactsAddedThisMonth(userDto.getUserId()));
		
		return "normalUser/contactsAddedThisMonth";
		
	}
	
	

}