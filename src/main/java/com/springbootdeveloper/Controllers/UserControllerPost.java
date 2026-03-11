package com.springbootdeveloper.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Mode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.DTO.UserDto;
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
                              Model model)
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