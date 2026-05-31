package com.springbootdeveloper.ServiceLayer;

import java.util.Optional;

import java.util.UUID;
import java.util.List;
import java.util.stream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.DTO.PasswordDto;
import com.springbootdeveloper.DTO.ProfileEnhanceDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.DTO.UserProfileDto;
import com.springbootdeveloper.Exceptions.ContactNotFoundException;
import com.springbootdeveloper.Exceptions.DuplicateEmailException;
import com.springbootdeveloper.Exceptions.UserNotFoundException;
import com.springbootdeveloper.Helpers.FileHandler;
import com.springbootdeveloper.JWTSecurityConfig.JwtUtil;
import com.springbootdeveloper.Models.Contact;
import com.springbootdeveloper.Models.User;
import com.springbootdeveloper.RepositoryLayer.DatabaseLayerUser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;



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
    @Autowired
    private JwtUtil jwtUtil;
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
        System.out.println("Here is pass during save"+user.getPassword());//$2a$10$dTWECoBq7Z.ELI6sQi4eQ.VIuooTN4Sgl2R8OJHWk5waIGQydXmcO
        return convertToDto(user);										  //$2a$10$dTWECoBq7Z.ELI6sQi4eQ.VIuooTN4Sgl2R8OJHWk5waIGQydXmcO
        																  //$2a$10$dTWECoBq7Z.ELI6sQi4eQ.VIuooTN4Sgl2R8OJHWk5waIGQydXmcO
    }
    	// ===== SAVE =====
    public UserDto save(UserDto userDto, ProfileEnhanceDto enhancedProfile)
    {
    	String fileName = "";
    	
    	User user = dl.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User Not Found")); //if its value give value , if empty optional then throw exception
   	 							//this is exception Supplier
    	
    	boolean ifUpdated = false;
    	
		if(enhancedProfile.getMultipartFile()!= null
				&& !enhancedProfile.getMultipartFile().isEmpty())
    	{
			fileName = fileHandler.upload(enhancedProfile.getMultipartFile());
			user.setImage(fileName);
			ifUpdated = true;
			
    	}
		
        if (enhancedProfile.getBio() != null && 
            !enhancedProfile.getBio().isBlank()) {
        	user.setBio(enhancedProfile.getBio());
        	ifUpdated = true;
           
        }
        
        
        if(ifUpdated)
        {
        	dl.save(user);
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
    
    
   
    public void autoLogin(UserDto dto, HttpServletResponse response) {

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                dto.getEmail(),
                dto.getPassword()
            );
        
        
        Authentication authentication =
            authenticationManager.authenticate(auth);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // ❌ Remove this session part as we dont need to save the sercurity context in the session— no sessions in JWT world
        // HttpSession session = request.getSession(true);
        // session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext);
        // Added this instead
        
        String token = jwtUtil.generateToken(authentication); //created the token so as to send it to the cliet as a cookie
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
        /*Added the cookie to the response ,now the browser makes sure that it sends this cookie with every request it makes*/
    }


	public UserProfileDto convertToUserProfileDto(UserDto user) 
	{
		
		UserProfileDto dto = new UserProfileDto();
		
	    dto.setUserId(user.getUserId());
	    dto.setName(user.getName());
	    dto.setEmail(user.getEmail());
	    dto.setImage(user.getImage());
	    dto.setBio(user.getBio());
	    dto.setFileName(user.getImage());
		return dto;
	}
	public UserProfileDto convertToUserProfileDto(User user) 
	{
		
		UserProfileDto dto = new UserProfileDto();
		
	    dto.setUserId(user.getUserId());
	    dto.setName(user.getName());
	    dto.setEmail(user.getEmail());
	    dto.setImage(user.getImage());
	    dto.setBio(user.getBio());
	    dto.setFileName(user.getImage());
		return dto;
	}
	
	
	/*
			if( newPassword !=null && !newPassword.isBlank() )
			{
				System.out.println("first one got executed");
				userDtoUpdated.setPassword(newPassword);
				autoLogin(userDtoUpdated,response);
			}
			else
			{
				//this case also includes the thing when we dont even have the password in field but something may have changed changed
				System.out.println("second one got executed");
				
				
			}
			System.out.println("partyOver");
			return userDtoUpdated; // can throw duplicate Contact Exception
			*/
	
	
	public UserProfileDto updateUser(UserDto userDto, UserProfileDto userProfileDto,HttpServletRequest request , HttpServletResponse response,boolean hasChanges)
	{
		
		System.out.println("Blockx1");
		String currentPassword = userProfileDto.getCurrentPassword();
		System.out.println("current userpass"+currentPassword);
		User user = dl.findByEmail(userDto.getEmail()).orElseThrow(() -> new UserNotFoundException("True Identity of user is not found")); 
		System.out.println("reached below findByEmail");
		if(hasChanges)
		{	
			 System.out.println("haschanges");
			//check password
			if (currentPassword == null || currentPassword.isBlank() ||
			        !passEncoder.matches(currentPassword, user.getPassword())) {
			        throw new BadCredentialsException("Current password is incorrect");
			    }
		     
		    //in case everything goes well,
		    
		    //in case user changes the mail to another
		    if (!user.getEmail().equalsIgnoreCase(userProfileDto.getEmail()) &&
		    	    dl.existsByEmail(userProfileDto.getEmail())) {
		    	    throw new DuplicateEmailException("Email already registered");
		    	}
			
		    System.out.println("Blockx2");    
		    //handling files 
			MultipartFile newFile = userProfileDto.getMultipartFile(); //will be empty if user didnt do anything
			String existingFileName = user.getImage(); // fetch from DB before update
			String newFileName = "";
				if (newFile != null && !newFile.isEmpty()) {
				    // ✅ FILE HAS HIGHEST PRIORITY
		
				    newFileName = fileHandler.upload(newFile);
				    System.out.println("new file got uploaded");
				    user.setImage(newFileName);
		
				    if (existingFileName != null && !existingFileName.isEmpty()) {
				        fileHandler.deleteIfExists(existingFileName);
				    }
				    System.out.println("No fileHandlerException");
				    System.out.println("checknewfile "+newFileName);
		
				} else if (userProfileDto.isRemovePhoto()) {
				    // ✅ REMOVE ONLY
		
				    if (existingFileName != null && !existingFileName.isEmpty()) {
				        fileHandler.deleteIfExists(existingFileName);
				    }
		
				    user.setImage(null);
		
				} 
				System.out.println("Blockx3");			
				user.setBio(userProfileDto.getBio());
				user.setEmail(userProfileDto.getEmail());
				user.setName(userProfileDto.getName());
				System.out.println("useremail"+user.getEmail());
				System.out.println("here is pas before"+user.getPassword());
	
				System.out.println("party");//everything went well
				UserDto userDtoUpdated = convertToDto(dl.save(user));
				UserProfileDto userProfileDtoUpdated = convertToUserProfileDto(userDtoUpdated);
				
				
				System.out.println("Here is pass after"+user.getPassword());
				System.out.println("Blockx4");
				userDtoUpdated.setPassword(	userProfileDto.getCurrentPassword());
				autoLogin(userDtoUpdated,response);
				System.out.println("Blockx5");
				return userProfileDtoUpdated;
		}
		else
			return convertToUserProfileDto(user);

	}


	public void updatePassword(UserDto userDto,HttpServletResponse response,PasswordDto passDto) {
		
		String currentPassword = passDto.getCurrentPassword();
		String newPassword = passDto.getNewPassword();
		String confirmPassword = passDto.getConfirmNewPassword();
		User user = dl.findByEmail(userDto.getEmail()).orElseThrow(() -> new UserNotFoundException("True Identity of user is not found"));
		
		
	    if ((currentPassword!=null && !currentPassword.isBlank()) &&!passEncoder.matches(currentPassword, user.getPassword())) {
	    	System.out.println("from1");
	        throw new BadCredentialsException("Current password is incorrect");
	    }
	    
	    if((currentPassword!=null && !currentPassword.isBlank()) &&passEncoder.matches(newPassword, user.getPassword())) {
	        throw new IllegalArgumentException("New password must be different from current password");
	    }
	    boolean updated = false;

	    if(!(currentPassword != null && !currentPassword.isBlank()
	    		   && newPassword != null && !newPassword.isBlank()
	    		   && confirmPassword != null && !confirmPassword.isBlank()
	    		   && newPassword.equals(confirmPassword))) {

	    		    throw new RuntimeException("Invalid password update request");
	    		}

	    		user.setPassword(passEncoder.encode(newPassword));
	    		userDto = convertToDto(dl.save(user));
	    		userDto.setPassword(newPassword);
	    		autoLogin(userDto,response);
	    

	}
}
   
    



