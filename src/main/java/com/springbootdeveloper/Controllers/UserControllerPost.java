package com.springbootdeveloper.Controllers;

import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Mode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.DTO.PasswordDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.DTO.UserProfileDto;
import com.springbootdeveloper.Exceptions.ContactNotFoundException;
import com.springbootdeveloper.Exceptions.DuplicateEmailException;
import com.springbootdeveloper.Exceptions.OwnerMismatchException;
import com.springbootdeveloper.Exceptions.UserNotFoundException;
import com.springbootdeveloper.ServiceLayer.ServiceClassContact;
import com.springbootdeveloper.ServiceLayer.ServiceClassUser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class UserControllerPost {

    @Autowired
    public ServiceClassContact serviceClassContact; // cant declare it static as Spring cant inject that if its static

    @Autowired
    public ServiceClassUser serviceClassUser;

    
    @PostMapping(path = "/user/addContactRequest")
    public String saveContact(@Valid @ModelAttribute("contactDto") ContactDto contactDto,
                              BindingResult results,
                              HttpServletRequest request,
                              Model model
                              )
    {

       
        UserDto userDto = serviceClassUser.findByEmail(request.getUserPrincipal().getName());
        if(results.hasErrors())
        {
            setPageAttributes(
                    model,
                    "dashboard",
                    "Dashboard",
                    "Hey " + userDto.getName() + "!!! Welcome to your User Dashboard",userDto
                    );
            return "normalUser/addContact";
        }
        
        try
        {
            serviceClassContact.saveContact(contactDto,userDto.getEmail());
        }
        catch (DataIntegrityViolationException ex) {
     
            results.reject("duplicateContact",
                    "Contact with same name, phone and group already exists in this group.");
            
            setPageAttributes(
                    model,
                    "dashboard",
                    "Dashboard",
                    "Hey " + userDto.getName() + "!!! Welcome to your User Dashboard",userDto
            );
            return "normalUser/addContact";
        }
        catch(RuntimeException e)
        {
            results.reject("multipartFile", e.getMessage());
            setPageAttributes(
                    model,
                    "dashboard",
                    "Dashboard",
                    "Hey " + userDto.getName() + "!!! Welcome to your User Dashboard",userDto
            );
            return "normalUser/addContact";
        }
        setPageAttributes(
                model,
                "dashboard",
                "Dashboard",
                "Hey " + userDto.getName() + "!!! Welcome to your User Dashboard",userDto
        );
        model.addAttribute("recentContacts", serviceClassContact.findLast10(userDto.getUserId()));
        model.addAttribute("favouritesCount",serviceClassContact.getFavouritesCount(userDto.getUserId()));
        model.addAttribute("totalContactsCount",serviceClassContact.getContactsCount(userDto.getUserId()));
    	model.addAttribute("contactsAddedThisMonth",serviceClassContact.getContactsAddedThisMonth(userDto.getUserId()));
        return "redirect:/user/dashboard";
    }

    @PostMapping("/user/editThisContact")
    public String handleUpdateContact(@Valid @ModelAttribute("contactDto")ContactDto contactDto,BindingResult results,Model model,Authentication authentication )
    {
    	
    	UserDto userDto = serviceClassUser.findByEmail(authentication.getName());
    	 if(results.hasErrors())
         {
             setPageAttributes(
                     model,
                     "contacts",
                     "Contact Edit Mode",
                     "Hey " + userDto.getName() + "!!! Here you can edit your contact as you like",userDto
                     );
             

             return "normalUser/editContact";
         }
    	 
    	 try
         {
 			if(!serviceClassContact.verifyContactOwner(contactDto.getContactId(),userDto.getUserId()))
 				throw new OwnerMismatchException("Ownership Mismatched");
             serviceClassContact.updateContact(contactDto);
         }

         catch(ContactNotFoundException e)
         {
        	 results.reject("NoSuchContactFound","No Such Contact in your Database");
        	 setPageAttributes(
                     model,
                     "contacts",
                     "Contact Edit Mode",
                     "Hey " + userDto.getName() + "!!! Here you can edit your contact as you like",userDto
                     );
        	 
             
        	 return "normalUser/editContact"; // contact is already bind with binding result
         }
         catch (DataIntegrityViolationException ex) {
         	System.out.println("--------------------------------");
         	System.out.println("Duplicate exception is caught");
         	System.out.println("--------------------------------");
             results.reject("duplicateContact",
                     "Contact with same name, phone and group already exists in this group.");
             
             setPageAttributes(
                     model,
                     "dashboard",
                     "Dashboard",
                     "Hey " + userDto.getName() + "!!! Welcome to your User Dashboard",userDto
             );

             return "normalUser/editContact";
         }

         catch(RuntimeException e)
         { // errors will be due to files only

             System.out.println("Catch----------------------------------------------");
             e.printStackTrace();

             results.reject("multipartFile", e.getMessage());

             setPageAttributes(
                     model,
                     "dashboard",
                     "Dashboard",
                     "Hey " + userDto.getName() + "!!! Welcome to your User Dashboard",userDto
             );

             return "normalUser/editContact";
         }
         


    	 setPageAttributes(
 				model,
 				"contacts",
 				"My Contacts Edit Mode",
 				"Hey "+userDto.getName()+"!!!Here are the contacts, modify as you like by clicking on edit icon",userDto
 		);
 		
 		model.addAttribute("userContacts",serviceClassContact.findAllContacts(userDto.getUserId()));
 		return "normalUser/editContacts";
     }
    	 
   @PostMapping(path = "/user/deleteThisContact/{contactId}")
    public String deleteContact(
        Authentication authentication, 
        RedirectAttributes redirectAttributes,
        @PathVariable UUID contactId) 
    {
        UserDto userDto = serviceClassUser.findByEmail(authentication.getName());
        ContactDto contactDto = null;
        
        try {
            if (contactId == null)
                throw new IllegalArgumentException("ContactId cannot be null");
                
            
            contactDto = serviceClassContact.getContactById(contactId);

            if (!serviceClassContact.verifyContactOwner(contactId, userDto.getUserId()))
                throw new OwnerMismatchException("Ownership Mismatched");

            serviceClassContact.deleteContact(contactId);
            
            return "redirect:/user/editContactsRequest";
            
        } catch (ContactNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Contact not found");
            
            return "redirect:/user/editContactsRequest";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid contact ID");
            
            return "redirect:/user/editContactsRequest";
        } catch (OwnerMismatchException e) {
        	
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to delete this contact");
            return "redirect:/user/editContactsRequest";
        } catch (OptimisticLockingFailureException e) {
        	
            redirectAttributes.addFlashAttribute("errorMessage", "Contact was already deleted");
            return "redirect:/user/editContactsRequest";
        } catch (Exception e) {
        	
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete contact");
            return "redirect:/user/editContactsRequest";
        }
    }
    
   
   @PostMapping("/logout")
   public String logout(HttpServletResponse response) {

       Cookie cookie = new Cookie("jwt", null);
       cookie.setHttpOnly(true); //cant be read with js
       cookie.setPath("/"); //looks for every path in browser
       cookie.setMaxAge(0);
       response.addCookie(cookie);
       return "redirect:/login";
   }
   

   
   @PostMapping("/user/updateProfileRequest")
   public String updateUserProfile(
           @Valid @ModelAttribute("userProfile") UserProfileDto userProfileDto,
           BindingResult result,
           HttpServletRequest request,
           HttpServletResponse response,
           Model model ,  RedirectAttributes redirectAttributes)
   {

       // 🔹 Get logged-in user
       UserDto userDto = serviceClassUser.findByEmail(request.getUserPrincipal().getName());
       System.out.println("ControllerBlock1");
       // 🔹 Fetch DB user (needed for comparison)
       UserDto existingUser = userDto;
               
       System.out.println("ControllerBlock2");
       // 🔹 Extract fields
       String currentPassword = userProfileDto.getCurrentPassword();
       String newName  = userProfileDto.getName();
       String newEmail = userProfileDto.getEmail();
       String newBio   = userProfileDto.getBio();
       System.out.println("current password is "+currentPassword);
       // 🔹 Detect changes
       boolean nameChanged  = !Objects.equals(existingUser.getName(), newName);
       boolean emailChanged = !existingUser.getEmail().equalsIgnoreCase(newEmail);
       boolean bioChanged   = !Objects.equals(existingUser.getBio(), newBio);
       
       
       boolean photoChanged =
               (userProfileDto.getMultipartFile() != null &&
                !userProfileDto.getMultipartFile().isEmpty())
               || userProfileDto.isRemovePhoto();  //either we have something in multipart file , or the flag got chagned(means field got changed)

       System.out.println("photo got chagned "+photoChanged);
       boolean hasChanges = nameChanged || emailChanged || bioChanged || photoChanged;

       // 🚨 STEP 1: If changes exist but password missing → bind error
       if (hasChanges && (currentPassword == null || currentPassword.isBlank())) {
           result.rejectValue(
                   "currentPassword",
                   "error.userProfile",
                   "Please enter your current password to save changes"
           );
       }//this verifies that user shouldnt leave currentPass field emtpy in case he changes something
       
       
       if (result.hasErrors()) {
    	   
    	    setPageAttributes(model,
    	        "dashboard",
    	        "User Profile",
    	        "Hey " + userDto.getName() + "!!! Here you can make changes in your Profile",
    	        userDto
    	    );
    	    System.out.println("ControllerBlock3");
    	    
    	    return "normalUser/userProfile";
    	}
       System.out.println("ControllerBlock4");
       
       
       UserProfileDto updatedUser;
       try
	   {
		   updatedUser = serviceClassUser.updateUser(userDto, userProfileDto,request,response,hasChanges);
		   System.out.println("Reached inside trycatch");
	   }
	   catch(BadCredentialsException e)
	   { 
		   setPageAttributes(model,
					"dashboard",
					"User Profile",
					"Hey "+userDto.getName()+"!!!Here you can make changes in your Profile",userDto
			);
		   result.rejectValue("currentPassword", "error.userProfile", "Incorrect password");
		   System.out.println("badcred");
		   return "normalUser/userProfile";
	   }
	   catch(DuplicateEmailException e)
	   {
		// Duplicate email
		   result.rejectValue("email", "error.userProfile", "Email already exists");
		   setPageAttributes(model,
					"dashboard",
					"User Profile",
					"Hey "+userDto.getName()+"!!!Here you can make changes in your Profile",userDto
			);
		   System.out.println("duplimail");
		   return "normalUser/userProfile";
		   
	   }
	   catch(RuntimeException e)
       { // errors will be due to files only          
		// File error
		   result.rejectValue("multipartFile", "error.userProfile", e.getMessage());
           setPageAttributes(model,
					"dashboard",
					"User Profile",
					"Hey "+userDto.getName()+"!!!Here you can make changes in your Profile",userDto
			);
           System.out.println("fileerror");
           return "normalUser/userProfile";
       }
       System.out.println("Reached out");
       redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
	   return "redirect:/user/viewProfileRequest";
   }
   
	   @PostMapping("/user/changePasswordRequest")
	   public String handlePassChange(Model model,@Valid @ModelAttribute("changePasswordDto") PasswordDto passDto, BindingResult result, HttpServletRequest request, RedirectAttributes redirectAttributes
			   										,HttpServletResponse response)
	   {
		   UserDto userDto = serviceClassUser.findByEmail(request.getUserPrincipal().getName());
		   String currentPassword = passDto.getCurrentPassword();
		   String newPassword = passDto.getNewPassword();
		   String confirmPassword = passDto.getConfirmNewPassword();
				   
		   if(currentPassword == null || currentPassword.isBlank())
		   {
			   //one of the field is filled
			   if( (newPassword !=null && !newPassword.isBlank() )||( confirmPassword !=null && !confirmPassword.isBlank()))
				   result.rejectValue(
						    "currentPassword",
						    "error.currentPassword.required",
						    "Enter the current password first to change the password"
						);
			   
			   if((newPassword == null || newPassword.isBlank()) && (confirmPassword ==null || confirmPassword.isBlank()))
			   {
				   redirectAttributes.addFlashAttribute("successMessage", "No changes made");
			   		return "redirect:/user/viewProfileRequest";
			   }
			 
		   }
		   else //current pass has something written in it
		   {
			   //one of the field is Empty
			   if( newPassword == null || newPassword.isBlank() || confirmPassword ==null || confirmPassword.isBlank())
			   {
				   result.rejectValue(
						    "newPassword",
						    "error.newPassword.required",
						    "Enter the new password and confirm it to change the password"
						);
			   }
			   else
			   {
				   if(!newPassword.equals(confirmPassword))
				   {
					   result.rejectValue(
							    "confirmNewPassword",
							    "error.password.mismatch",   // ← code
							    "The new password must match the confirmed password" // ← message
							);
				   }
			   }
		   }
		   
		   if(result.hasErrors())
		   {
			   setPageAttributes(model,
						"dashboard",
						"User Profile",
						"Hey "+userDto.getName()+"!!!Here you can make changes in your Profile Password",userDto
				);
			   return "normalUser/changePassword";
		   }
		   
		   try
		   {
			   serviceClassUser.updatePassword(userDto,response,passDto);
		   }
		   catch(BadCredentialsException e)
		   {
			   setPageAttributes(model,
						"dashboard",
						"User Profile",
						"Hey "+userDto.getName()+"!!!Here you can make changes in your Profile Password",userDto
				);
			   result.rejectValue(
					    "currentPassword",
					    "error.currentPassword.invalid",
					    "Current Password is incorrect"
					);
			   return "normalUser/changePassword";
		   }
		   catch(IllegalArgumentException e)
		   {
			   setPageAttributes(model,
						"dashboard",
						"User Profile",
						"Hey "+userDto.getName()+"!!!Here you can make changes in your Profile Password",userDto
				);
		       result.rejectValue("newPassword", null, e.getMessage());
		       return "normalUser/changePassword";
		   }
		   catch(RuntimeException e)
		   {
			   setPageAttributes(model,
						"dashboard",
						"User Profile",
						"Hey "+userDto.getName()+"!!!Here you can make changes in your Profile Password",userDto
				);
			   redirectAttributes.addFlashAttribute("errorMessage", "Some Error with the server");
			   return "redirect:/user/viewProfileRequest";
		   }
		   setPageAttributes(model,
					"dashboard",
					"User Profile",
					"Hey "+userDto.getName()+"!!!Here you can make changes in your Profile Password",userDto
			);
		   redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully");
		   return "redirect:/user/viewProfileRequest";
		   
	   }
    
    // reusable helper method
    private void setPageAttributes(Model model,
                                   String activePage,
                                   String pageTitle,
                                   String pageSubtitle,UserDto userDto)
    {
        model.addAttribute("activePage", activePage);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("pageSubtitle", pageSubtitle);
        model.addAttribute("user",userDto);
    }
    
    
    

}