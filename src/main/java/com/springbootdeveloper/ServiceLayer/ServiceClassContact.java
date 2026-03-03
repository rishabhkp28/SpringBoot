package com.springbootdeveloper.ServiceLayer;

import org.springframework.stereotype.Service;
import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.Helpers.FileHandler;
import com.springbootdeveloper.Models.Contact;
import com.springbootdeveloper.RepositoryLayer.DatabaseLayerContacts;

@Service
public class ServiceClassContact {
	
	private DatabaseLayerContacts dbContacts;
	private FileHandler fileHandler;
	
	public ServiceClassContact(DatabaseLayerContacts dbContacts, FileHandler fileHandler)
	{
		this.dbContacts = dbContacts;
		this.fileHandler = fileHandler;
	}
	//Avoiding optional as its not a good practie to send optional from service to controller
	//but we can send from db to service
	
	public ContactDto saveContact(ContactDto contactDto)throws RuntimeException
	{
		
	    	String fileName = "";
	    
			if(contactDto.getImage() == null
					&& !contactDto.getImage().isEmpty())
	    	{
				fileName = fileHandler.upload(contactDto.getImage());
				contactDto.setFileName(fileName);
					
	    	}
			
			Contact contact = convertToEntity(contactDto);
			
			dbContacts.save(contact);
			
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
	    dto.setCreatedDate(contact.getCreatedDate());
	    dto.setUpdatedDate(contact.getUpdatedDate());

	    return dto;
	}
	
}
