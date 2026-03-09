package com.springbootdeveloper.ServiceLayer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.DTO.UserDto;
import com.springbootdeveloper.Helpers.FileHandler;
import com.springbootdeveloper.Models.Contact;
import com.springbootdeveloper.Models.User;
import com.springbootdeveloper.RepositoryLayer.DatabaseLayerContacts;
import com.springbootdeveloper.RepositoryLayer.DatabaseLayerUser;

@Service
public class ServiceClassContact {
	
	private DatabaseLayerContacts dbContacts;
	private FileHandler fileHandler;
	private DatabaseLayerUser dbUser;
	
	public ServiceClassContact(DatabaseLayerContacts dbContacts, FileHandler fileHandler, DatabaseLayerUser dbUser)
	{
		this.dbContacts = dbContacts;
		this.fileHandler = fileHandler;
		this.dbUser =  dbUser;
	}
	//Avoiding optional as its not a good practie to send optional from service to controller
	//but we can send from db to service
	
	public ContactDto saveContact(ContactDto contactDto, String email)throws RuntimeException
	{
		
		
		
	    	String fileName = "";
	    
	    	if(contactDto.getMultipartFile() != null && !contactDto.getMultipartFile().isEmpty()) {
	    	    fileName = fileHandler.upload(contactDto.getMultipartFile());
	    	    contactDto.setFileName(fileName);
	    	}
			
		
			User user = dbUser.findByEmail(email).get();
			
			
			Contact contact = convertToEntity(contactDto);
			
			contact.setUser(user);
			
			dbContacts.save(contact);
			user.addContact(contact);
			
			return convertToDto(contact);
	 }
	

	public Contact convertToEntity(ContactDto contactDto) {

	    if (contactDto == null) {
	        return null;
	    }

	    Contact contact = new Contact();

	    contact.setContactId(contactDto.getContactId()); // For update case
	    contact.setName(contactDto.getName());
	    contact.setNickName(contactDto.getNickName());
	    contact.setDescription(contactDto.getDescription());
	    contact.setPhone(contactDto.getPhone());
	    contact.setEmail(contactDto.getEmail());
	    contact.setImage(contactDto.getFileName());
	    contact.setFavourite(contactDto.getFavourite());
	    contact.setGroup(contactDto.getGroup());

	    // DO NOT set createdDate manually (handled by @PrePersist)
	    // updatedDate handled by @PreUpdate

	    return contact;
	}
	
	public ContactDto convertToDto(Contact contact) { // cant send multipart to front End

	    if (contact == null) {
	        return null;
	    }

	    ContactDto dto = new ContactDto();

	    dto.setContactId(contact.getContactId());
	    dto.setName(contact.getName());
	    dto.setNickName(contact.getNickName());
	    dto.setDescription(contact.getDescription());
	    dto.setPhone(contact.getPhone());
	    dto.setEmail(contact.getEmail());
	    dto.setFileName(contact.getImage());
	    dto.setGroup(contact.getGroup());
	    dto.setFavourite(contact.getFavourite());
	    dto.setCreatedDate(contact.getCreatedDate());
	    dto.setUpdatedDate(contact.getUpdatedDate());

	    return dto;
	}
	
	public List<ContactDto> findLast10(UUID id)
	{
		
		return dbContacts.findTop10ByUser_UserIdOrderByUpdatedDateDesc(id).stream().map(x -> convertToDto(x)).collect(Collectors.toList());	
	}
	
	
	
	
	
}
