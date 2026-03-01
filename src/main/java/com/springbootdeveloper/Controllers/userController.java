package com.springbootdeveloper.Controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.springbootdeveloper.ServiceLayer.ServiceClass;
import com.springbootdeveloper.DTO.UserDto;


@Controller
public class userController {
	
	
	private ServiceClass serviceClass;
	
	public userController(ServiceClass serviceClass)
	{
		this.serviceClass = serviceClass;
	}
	
	@GetMapping(path ="/user/dashboard")
	public String displayDashboard(Authentication authentication,Model model)
	{
		/*This check is not needed as we have handled it in the Spring Security Configuration File .....
		if (authentication == null || !authentication.isAuthenticated()) {
			System.out.println("Login is Failed");
		    return "redirect:/login";
		}*/
		
		UserDto userDto = null;
		userDto = serviceClass.findByEmail(authentication.getName());
    	
		model.addAttribute("user",userDto);
		model.addAttribute("activePage","dashboard");
		model.addAttribute("pageTitle","Dashboard");
		model.addAttribute("pageSubtitle","Hey "+userDto.getName()+"!!! Welcome to your User Dashboard");
		return "normalUser/userDashboard";
		/*Send the variables that are defined in the basepage
		 * send activePage name ( dashboard , contacts ,favourites , groups ,imports )
		 * pageTitle
		 * pageSubtitle
		 * details of user like email, profilePicUrl , namee  -> all these are already present in the DTO
		 * */
	}
	@GetMapping(path ="/user/contacts")
	public String displayContacts(Authentication authentication,Model model)
	{
		UserDto userDto = null;
		userDto = serviceClass.findByEmail(authentication.getName());
    	
		model.addAttribute("User",userDto);
		model.addAttribute("activePage","contacts");
		model.addAttribute("pageTitle","Contacts");
		model.addAttribute("pageSubtitle","Hey "+userDto.getName()+"!!! Below shows your list of contacts saved on our cloud");
		return "normalUser/userContacts";
		
	}
	@GetMapping(path ="/user/favourites") // will implement this functionality 
	public String displayFavourites(Authentication authentication,Model model)
	{
		UserDto userDto = null;
		userDto = serviceClass.findByEmail(authentication.getName());
    	
		model.addAttribute("User",userDto);
		model.addAttribute("activePage","favourites");
		model.addAttribute("pageTitle","Favourites");
		model.addAttribute("pageSubtitle","Hey "+userDto.getName()+"!!! Below shows your favourite contacts saved on our cloud");
		return "normalUser/userFavourites";
		
	}
	
	@GetMapping(path ="/user/groups")//will implement this later
	public String displayGroups(Authentication authentication,Model model)
	{
		UserDto userDto = null;
		userDto = serviceClass.findByEmail(authentication.getName());
    	
		model.addAttribute("User",userDto);
		model.addAttribute("activePage","groups");
		model.addAttribute("pageTitle","Groups");
		model.addAttribute("pageSubtitle","Hey "+userDto.getName()+"!!! Below shows your customized Groups");
		return "normalUser/userGroups";
		
	}
	@GetMapping(path ="/user/import")
	public String displayImport(Authentication authentication,Model model)
	{
		
		UserDto userDto = null;
		userDto = serviceClass.findByEmail(authentication.getName());
    	
		model.addAttribute("User",userDto);
		model.addAttribute("activePage","import");
		model.addAttribute("pageTitle","Import");
		model.addAttribute("pageSubtitle","Hey "+userDto.getName()+"!!! Below are the import functionalities available");
		return "normalUser/userImport";
		
	}
	@GetMapping(path ="/user/profile")
	public String displaProfile(Authentication authentication,Model model)
	{
		UserDto userDto = null;
		userDto = serviceClass.findByEmail(authentication.getName());
    	
		model.addAttribute("User",userDto);
		model.addAttribute("activePage","profile");
		model.addAttribute("pageTitle","Profile");
		model.addAttribute("pageSubtitle","Hey "+userDto.getName()+"!!! Below is your Profile on our server");
		return "normalUser/userProfile";
		
	}
	@GetMapping(path ="/user/settings")
	public String displaySettings(Authentication authentication,Model model)
	{
		
		UserDto userDto = null;
		userDto = serviceClass.findByEmail(authentication.getName());
    	
		model.addAttribute("User",userDto);
		model.addAttribute("activePage","settings");
		model.addAttribute("pageTitle","Settings");
		model.addAttribute("pageSubtitle","Hey "+userDto.getName()+"!!!Configure as you want");
		return "normalUser/userSettings";
		
	}
}
