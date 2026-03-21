package com.springbootdeveloper.Controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Mode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.Exceptions.ContactNotFoundException;
import com.springbootdeveloper.Exceptions.OwnerMismatchException;
import com.springbootdeveloper.Exceptions.UserNotFoundException;
import com.springbootdeveloper.ServiceLayer.ServiceClassContact;
import com.springbootdeveloper.ServiceLayer.ServiceClassUser;

import jakarta.servlet.http.HttpServletRequest;
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

        String email = "";
        System.out.println("==================================================================");

        UserDto userDto = null;

        try
        {
            email = request.getUserPrincipal().getName();
            userDto = serviceClassUser.findByEmail(email);
        }
        catch(UserNotFoundException e)
        {
            System.out.println("User has not been found");
            return "redirect:/logout";
        }


        // default validation first

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
            serviceClassContact.saveContact(contactDto,email);
        }

        catch(UserNotFoundException e)
        {
            System.out.println("User has not been found");
            return "redirect:/logout";
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

            return "normalUser/addContact";
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

            return "normalUser/addContact";
        }
        


        setPageAttributes(
                model,
                "dashboard",
                "Dashboard",
                "Hey " + userDto.getName() + "!!! Welcome to your User Dashboard",userDto
        );

        model.addAttribute(
                "recentContacts",
                serviceClassContact.findLast10(userDto.getUserId())
        );
        model.addAttribute("favouritesCount",serviceClassContact.getFavouritesCount(userDto.getUserId()));
        model.addAttribute("totalContactsCount",serviceClassContact.getContactsCount(userDto.getUserId()));
    	model.addAttribute("contactsAddedThisMonth",serviceClassContact.getContactsAddedThisMonth(userDto.getUserId()));
        return "redirect:/user/dashboard";
    }


    
    @PostMapping("/user/editThisContact")
    public String handleUpdateContact(@Valid @ModelAttribute("contactDto")ContactDto contactDto,BindingResult results,Model model,Authentication authentication )
    {
    	
    	UserDto userDto = null;
    	try
    	{
    		userDto = serviceClassUser.findByEmail(authentication.getName());
    	}
    	catch(UserNotFoundException e)
        {
            System.out.println("User has not been found");
            return "redirect:/logout";
        }
    	
    	
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
    		userDto = serviceClassUser.findByEmail(authentication.getName());

 			if(!serviceClassContact.verifyOwner(contactDto.getContactId(),userDto.getUserId()))
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
        UserDto userDto = null;
        ContactDto contactDto = null;
        
        try {
            if (contactId == null)
                throw new IllegalArgumentException("ContactId cannot be null");
                
            userDto = serviceClassUser.findByEmail(authentication.getName());
            contactDto = serviceClassContact.getContactById(contactId);

            if (!serviceClassContact.verifyOwner(contactId, userDto.getUserId()))
                throw new OwnerMismatchException("Ownership Mismatched");

            serviceClassContact.deleteContact(contactId);
            System.out.println("Here is Executed");
            return "redirect:/user/editContactsRequest";
            
        } catch (UserNotFoundException e) {
        	System.out.println("Here is no 1");
            return "redirect:/logout";
        } catch (ContactNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Contact not found");
            System.out.println("Here is no 2");
            return "redirect:/user/editContactsRequest";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid contact ID");
            System.out.println("Here is no 3");
            return "redirect:/user/editContactsRequest";
        } catch (OwnerMismatchException e) {
        	System.out.println("Here is no 4");
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to delete this contact");
            return "redirect:/user/editContactsRequest";
        } catch (OptimisticLockingFailureException e) {
        	System.out.println("Here is no 5");
            redirectAttributes.addFlashAttribute("errorMessage", "Contact was already deleted");
            return "redirect:/user/editContactsRequest";
        } catch (Exception e) {
        	System.out.println("Here is no 6");
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete contact");
            return "redirect:/user/editContactsRequest";
        }
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