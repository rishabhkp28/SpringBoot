package com.springbootdeveloper.Controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.springbootdeveloper.ServiceLayer.ServiceClassContact;
import com.springbootdeveloper.ServiceLayer.ServiceClassUser;

import jakarta.servlet.http.HttpServletRequest;

import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.DTO.PasswordDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.DTO.UserProfileDto;
import com.springbootdeveloper.Exceptions.ContactNotFoundException;
import com.springbootdeveloper.Exceptions.OwnerMismatchException;
import com.springbootdeveloper.Exceptions.UserNotFoundException;
import com.springbootdeveloper.Helpers.FileHandler;
import com.springbootdeveloper.Helpers.VCFExportService;
import com.springbootdeveloper.Models.ContactGroup;

@Controller
public class UserControllerGet {
	
	
	private ServiceClassUser serviceClassUser;
	private ServiceClassContact serviceClassContact;
	private FileHandler fileHandler;
	private VCFExportService vCFExportService;
	
	public UserControllerGet(ServiceClassUser serviceClassUser,ServiceClassContact serviceClassContact,FileHandler fileHandler,VCFExportService vCFExportService)
	{
		this.serviceClassUser = serviceClassUser;
		this.serviceClassContact = serviceClassContact;
		this.fileHandler = fileHandler;
		this.vCFExportService = vCFExportService;
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
		
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());
		long favourites =  serviceClassContact.getFavouritesCount(userDto.getUserId());
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
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());

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
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());
					
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
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());setPageModel(
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
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());

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
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());
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
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());

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
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());

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
		
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());
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
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());
					setPageModel(
							model,
							"dashboard",
							"Dashboard",
							"Hey "+userDto.getName()+"!!!Here are the contacts that you added this month",userDto
					);

					

		model.addAttribute("contactsAddedThisMonth", serviceClassContact.getContactsAddedThisMonth(userDto.getUserId()));
		
		return "normalUser/contactsAddedThisMonth";
		
	}
	
	
	@GetMapping(path = "/user/editContactsRequest")
	public String editContacts(Authentication authentication, Model model)
	{
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());
		
		setPageModel(
				model,
				"contacts",
				"My Contacts Edit Mode",
				"Hey "+userDto.getName()+"!!!Here are the contacts, modify as you like by clicking on edit icon",userDto
		);
		
		model.addAttribute("userContacts",serviceClassContact.findAllContacts(userDto.getUserId()));
		return "normalUser/editContacts";
	}
	@GetMapping(path = "/user/editContact/{contactId}")
	public String editContact(Authentication authentication, Model model,@PathVariable("contactId") UUID contactId)
	{
		UserDto userDto = serviceClassUser.findByEmail(authentication.getName());
		ContactDto contactDto = null;
		try
		{
			contactDto = serviceClassContact.getContactById(contactId);
			if(!serviceClassContact.verifyContactOwner(contactId,userDto.getUserId()))
					throw new OwnerMismatchException("Ownership Mismatched");
			
			System.out.println("Contact is Verified");
		}
		catch(ContactNotFoundException e)
		{
			return "redirect:/user/editContactsRequest?error=True";
		}
		catch(OwnerMismatchException e)
		{
			
			return "redirect:/user/editContactsRequest?error=True";
		}	
		setPageModel(
				model,
				"dashboard",
				"Dashboard",
				"Hey "+userDto.getName()+"!!!Here are the contacts that you added this month",userDto
		);
		
		model.addAttribute("contactDto",contactDto);
		
		return "normalUser/editContact";
	}
	

	@GetMapping("/contact/images/{contactId}") /*Caution as this can bypass the security for image checking*/
    public ResponseEntity<Resource> serveImage(
            @PathVariable("contactId") UUID contactId, Authentication authentication) {
		
		UserDto userDto =  serviceClassUser.findByEmail(authentication.getName());
		ContactDto contactDto = null;
		String fileName = null;
		
		try
		{
			
			contactDto = serviceClassContact.getContactById(contactId);
			fileName = contactDto.getFileName();
			
			if(!serviceClassContact.verifyContactOwner(contactId,userDto.getUserId()))
				throw new OwnerMismatchException("Ownership Mismatched");	
		}
		
		catch(ContactNotFoundException e)
		{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		catch(OwnerMismatchException e)
		{
		    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

        // FileHandler.getFile() already has path traversal protection
        Resource resource = fileHandler.getFile(fileName);
        System.out.println("file name is "+fileName);
 
        // detect content type from file extension
        String contentType;
        try {
            contentType = Files.probeContentType(Paths.get(fileName));
        } catch (IOException e) {
            contentType = null;
        }
 
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
 
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
	
	@GetMapping("/user/images/{contactId}") /*Caution as this can bypass the security for image checking*/
    public ResponseEntity<Resource> serveUserImage(Authentication authentication) {
		
		UserDto userDto =  serviceClassUser.findByEmail(authentication.getName());
		String imageName;
		
			imageName = userDto.getImage();
		
		
		// FileHandler.getFile() already has path traversal protection
        Resource resource = fileHandler.getFile(imageName);
 
        // detect content type from file extension
        String contentType;
        try {
            contentType = Files.probeContentType(Paths.get(imageName));
        } catch (IOException e) {
            contentType = null;
        }
 
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
 
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
	
	
	@GetMapping(path = "/user/viewProfileRequest")
	public String displayProfile(HttpServletRequest request,Model model)
	{
		UserDto userDto = serviceClassUser.findByEmail(request.getUserPrincipal().getName());
		
		
		/*This conversion is needed as we need to make sure he validations match the newer one*/
		
		UserProfileDto userProfileDto = serviceClassUser.convertToUserProfileDto(userDto);
		System.out.println("Here is the filename "+userProfileDto.getFileName());
		model.addAttribute("userDto",userDto);
		setPageModel(
				model,
				"dashboard",
				"User Profile",
				"Hey "+userDto.getName()+"!!!Here you can make changes in your Profile",userDto
		);
		model.addAttribute("userProfile",userProfileDto);
		return "normalUser/userProfile";
	}
	@GetMapping(path = "/user/changePasswordRequest")
	public String loadChangePasswordModule(Model model, Authentication authentication)
	{
		
		UserDto userDto =  serviceClassUser.findByEmail(authentication.getName());
		model.addAttribute("userDto",userDto);
		setPageModel(
				model,
				"dashboard",
				"User Profile",
				"Hey "+userDto.getName()+"!!!Here you can make changes in your Profile",userDto
		);
		
		model.addAttribute("changePasswordDto",new PasswordDto());
		return "normalUser/changePassword";
	}
	
	@GetMapping("/user/export")
	public ResponseEntity<byte[]> exportContacts(Principal principal) {

	    String email = principal.getName();
	    UserDto userDto = serviceClassUser.findByEmail(email);

	    byte[] vcfBytes =
	            vCFExportService.exportUserContacts(userDto.getContactDtos());

	    HttpHeaders headers = new HttpHeaders();

	    headers.setContentType(
	            MediaType.parseMediaType("text/vcard")
	    );

	    headers.setContentDisposition(
	            ContentDisposition
	                    .attachment()
	                    .filename("contacts.vcf")
	                    .build()
	    );

	    return ResponseEntity
	            .ok()
	            .headers(headers)
	            .body(vcfBytes);
	    
	    
	}
	

	
	
}