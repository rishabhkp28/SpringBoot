package com.springbootdeveloper.ServiceLayer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.stream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.DTO.ProfileEnhanceDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.Exceptions.DuplicateEmailException;
import com.springbootdeveloper.Exceptions.UserNotFoundException;
import com.springbootdeveloper.Helpers.FileHandler;
import com.springbootdeveloper.Models.Contact;
import com.springbootdeveloper.Models.User;
import com.springbootdeveloper.RepositoryLayer.DatabaseLayerUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
public class ServiceClassUser {//defined for rules and regulations regarding the business

    @Autowired
    private  DatabaseLayerUser dl; // didnt made this final ,as it would require initial value or constructor initializatoin, for autowired i dont need that
    
    private final BCryptPasswordEncoder passEncoder;
    private final AuthenticationManager authenticationManager;
    @Autowired
    private FileHandler fileHandler;//Spring injects it via reflection
    @Autowired
    private ServiceClassContact serviceClassContact;
    
    //Spring injects it from Spring security
    public ServiceClassUser(BCryptPasswordEncoder passEncoder,AuthenticationManager authenticationManager)
    {
    	this.passEncoder = passEncoder;
    	this.authenticationManager = authenticationManager;
    }
    

    // ===== FIND =====
    public UserDto findById(UUID id) {
    	
    	Optional<User> optionalUser = dl.findById(id);

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        User user = optionalUser.get();

        return convertToDto(user);
       
    }

    public UserDto findByEmail(String email) {
    	
    	Optional<User> optionalUser = dl.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        User user = optionalUser.get();

        return convertToDto(user);
       
    }
 
    // ===== SAVE =====
    public UserDto save(UserDto dto) {

        // Email uniqueness check
        if (dl.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email already registered");
        }
        dto.setRole("USER");
        
        User user =	convertToEntity(dto);
        
      
        user = dl.save(user);
        return convertToDto(user);
    }
    	// ===== SAVE =====
    public UserDto save(UserDto userDto, ProfileEnhanceDto enhancedProfile)throws RuntimeException 
    {
    	String fileName = "";
    	
    	
    	User user = dl.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User Not Found")); //if its value give value , if empty optional then throw exception
   	 							//this is exception Supplier
    	
		if(enhancedProfile.getMultipartFile()!= null
				&& !enhancedProfile.getMultipartFile().isEmpty())
    	{
			fileName = fileHandler.upload(enhancedProfile.getMultipartFile());
			user.setImage(fileName);
			
    	}
		
        if (enhancedProfile.getBio() != null && 
            !enhancedProfile.getBio().isBlank()) {
        	user.setBio(enhancedProfile.getBio());
        	 
           
        }
        
        return convertToDto(user);
    }
    
    public boolean checkExisting(String email)
    {
    	return dl.existsByEmail(email);
    }
    
    
     public UserDto update(UserDto dto)//the user must exist already for this or it throws user not found exception
    {	
    	 
    	 
    	 User user = dl.findByEmail(dto.getEmail())
                 .orElseThrow(() -> new UserNotFoundException("User Not Found")); //if its value give value , if not then throw exception
    	 							//this is exception Supplier
    	 
    	user.setImage(dto.getImage());	 
     	user.setName(dto.getName());
     	user.setEmail(dto.getEmail());
     	user.setRole(dto.getRole());
     	user.setEnable(dto.isEnable());
     	user.setImage(dto.getImage());
     	user.setBio(dto.getBio());
    	return convertToDto(user);
    	 
    }    
    
    
    private UserDto convertToDto(User user) {//internal use , converts to dto(no pass) that can be sent to screen frontend
		   	UserDto dto = new UserDto();
		    dto.setUserId(user.getUserId());
		    dto.setName(user.getName());
		    dto.setEmail(user.getEmail());
		    dto.setRole(user.getRole());
		    dto.setEnable(user.isEnable());
		    dto.setImage(user.getImage());
		    dto.setBio(user.getBio());
		    
		    
		    List<Contact> contacts = user.getContacts();
		    
		    List<ContactDto> contactDtos = contacts.stream().map(contact -> serviceClassContact.convertToDto(contact)).collect(Collectors.toList());
   
		    dto.setContactDtos(contactDtos); //converted them to contactDtos , so as to send to the user
			return dto;
	}
    
    private User convertToEntity(UserDto userdata)
    {
    	User user = new User();
    	user.setPassword(passEncoder.encode(userdata.getPassword()));
    	user.setUserId(userdata.getUserId());
    	user.setUserId(userdata.getUserId());
    	user.setName(userdata.getName());
    	user.setEmail(userdata.getEmail());
    	user.setRole(userdata.getRole());
    	user.setEnable(userdata.isEnable());
    	user.setImage(userdata.getImage());
    	user.setBio(userdata.getBio());
    	    	
    	return user;

    }
    
    
    
    public void autoLogin(UserDto dto, HttpServletRequest request)
    {//this dto  has the pass to autologin on first time registration
    	
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                dto.getEmail(),
                dto.getPassword()
            );//verification of pass and email

        Authentication authentication =
            authenticationManager.authenticate(auth);//login request

        SecurityContextHolder.getContext()
                             .setAuthentication(authentication);//gets stored in current thread only
        /*Redirect = new HTTP request. and hence a new thread is created
        */
        
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, //this takes our authentication object and stores it in our HttpSession
                SecurityContextHolder.getContext()
        );
    }
    
    public List<ContactDto> getFavouriteContacts(UserDto userDto)
    {
    	
    	List<ContactDto> contactDtos = userDto.getContactDtos();
    	contactDtos = contactDtos.stream().filter(contactDto -> contactDto.getFavourite() == true).collect(Collectors.toList());
    	
    	return contactDtos;
    	
    }
    
    
    public long getFavouritesCount(UserDto userDto)
    {
    	long  favourites = this.getFavouriteContacts(userDto).size();
    	
    	System.out.println(favourites);
    	System.out.println("-----------------------------------------------------"+favourites);
    	
    	return favourites;

    }
    
    
 

}
