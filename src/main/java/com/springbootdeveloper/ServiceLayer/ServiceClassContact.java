package com.springbootdeveloper.ServiceLayer;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springbootdeveloper.DTO.ContactDto;
import com.springbootdeveloper.Exceptions.ContactNotFoundException;
import com.springbootdeveloper.Exceptions.UserNotFoundException;
import com.springbootdeveloper.Helpers.FileHandler;
import com.springbootdeveloper.Models.Contact;
import com.springbootdeveloper.Models.ContactGroup;
import com.springbootdeveloper.Models.User;
import com.springbootdeveloper.RepositoryLayer.DatabaseLayerContacts;
import com.springbootdeveloper.RepositoryLayer.DatabaseLayerUser;


import org.springframework.transaction.annotation.Transactional;  // ✅ CORRECT


@Transactional
@Service
public class ServiceClassContact {
	
	private DatabaseLayerContacts dbContacts;
    private DatabaseLayerUser dbUser;

    private FileHandler fileHandler;
    
    public ServiceClassContact(
        DatabaseLayerContacts dbContacts, 
        FileHandler fileHandler, 
        DatabaseLayerUser dbUser
        )  // ← Add this to constructor
    {
        this.dbContacts = dbContacts;
        this.fileHandler = fileHandler;
        this.dbUser = dbUser;
        // ← Add this
    }
	//Avoiding optional as its not a good practie to send optional from service to controller
	//but we can send from db to service
	public ContactDto getContactById(UUID id)throws RuntimeException
	{
		
		Contact contact = dbContacts.findById(id).orElseThrow(() ->new ContactNotFoundException("Contact is not Found"));
		
		return convertToDto(contact);
		
	}
	
	public ContactDto saveContact(ContactDto contactDto, String email)throws RuntimeException
	{
		
	    	String fileName = "";
	    
	    	if(contactDto.getMultipartFile() != null && !contactDto.getMultipartFile().isEmpty()) {
	    	    fileName = fileHandler.upload(contactDto.getMultipartFile());
	    	    contactDto.setFileName(fileName);
	    	}
			
		
			User user = dbUser.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User has not been found"));
			
			
			Contact contact = convertToEntity(contactDto);
			
			contact.setUser(user);
			
			dbContacts.save(contact);
			user.addContact(contact); //similarly adding to contacts of the user
			
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
	
	/*Summary Table  received from front end
Case                         fileInput   removePhotoFlag     Server action
Newfile                       hasfile     false               delete old → save new
Remove clicked               empty        true                delete old → null
Nothing done                emptyfalse     keep               existing*/
	
	public ContactDto updateContact(ContactDto contactDto)throws RuntimeException //for handling exceptions caused by fileHandler
	{
		Contact existingContact = null;
		existingContact = dbContacts.findById(contactDto.getContactId()).orElseThrow(
						()-> new ContactNotFoundException("No existing contact found")); //for caused during find
			

		MultipartFile newFile = contactDto.getMultipartFile(); //will be empty if user didnt do anything
		String existingFileName = existingContact.getImage(); // fetch from DB before update
		String newFileName = "";
			if (newFile != null && !newFile.isEmpty()) {
			    // ✅ FILE HAS HIGHEST PRIORITY
	
			    newFileName = fileHandler.upload(newFile);
			    existingContact.setImage(newFileName);
	
			    if (existingFileName != null && !existingFileName.isEmpty()) {
			        fileHandler.deleteIfExists(existingFileName);
			    }
	
			} else if (contactDto.isRemovePhoto()) {
			    // ✅ REMOVE ONLY
	
			    if (existingFileName != null && !existingFileName.isEmpty()) {
			        fileHandler.deleteIfExists(existingFileName);
			    }
	
			    existingContact.setImage(null);
	
			} 
				

			existingContact.setDescription(contactDto.getDescription());
			existingContact.setEmail(contactDto.getEmail());
			existingContact.setNickName(contactDto.getNickName());
			existingContact.setPhone(contactDto.getPhone());
			existingContact.setGroup(contactDto.getGroup());
			existingContact.setName(contactDto.getName());
			existingContact.setFavourite(contactDto.getFavourite());

				
			return convertToDto(dbContacts.save(existingContact)); // can throw duplicate Contact Exception

	}

	public List<ContactDto> findLast10(UUID id)
	{
		
		return dbContacts.findTop10ByUser_UserIdOrderByUpdatedDateDesc(id).stream().map(x -> convertToDto(x)).collect(Collectors.toList());	
	}
	
	
	public List<ContactDto> findAllContacts(UUID id)
	{
		
		return dbContacts.findByUser_UserId(id).stream().map(x-> convertToDto(x)).collect(Collectors.toList());
	}

	public List<ContactDto> getFavouriteContacts(UUID id)
    {
    	
    	    	
    	return dbContacts.findFavouriteContacts(id).stream().map(x -> convertToDto(x)).collect(Collectors.toList());
    	
    }

	public List<ContactDto> getContactsAddedThisMonth(UUID id)
	{
		LocalDateTime startOfMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
			
			LocalDateTime now = LocalDateTime.now();
			
			List<Contact> contacts = dbContacts
			.findByUser_UserIdAndCreatedDateBetween(id, startOfMonth, now);
			
			return contacts.stream().map(x -> convertToDto(x)).collect(Collectors.toList());
	}
	
	
	
	public List<ContactDto> getContactsByGroup(UUID userId,ContactGroup contactGroup)
	{
		
		List<Contact> contacts = dbContacts.findByUser_UserIdAndGroup(userId, contactGroup);
		
		return contacts.stream().map(x-> convertToDto(x)).collect(Collectors.toList());
		
	}
	
	public long getCountByGroup(UUID userId,ContactGroup contactGroup)
	{
		
		
		return dbContacts.countByUser_UserIdAndGroup(userId, contactGroup);
	}
	
	
    public long getFavouritesCount(UUID userId)
    {
    	
    	return  dbContacts.findFavouriteCount(userId);
    	
    }
  
    public long getContactsCount(UUID userId)
    {
    	return dbContacts.countByUser_UserId(userId);
    }
   
    public long getThisMonthContactCount(UUID userId)
    {
    	LocalDateTime startOfMonth = LocalDate.now()
                .withDayOfMonth(1).atStartOfDay();
                
    	LocalDateTime now = LocalDateTime.now();
    	return dbContacts.countByUser_UserIdAndCreatedDateBetween(userId, startOfMonth, now);
    }
 
    public boolean verifyContactOwner(UUID contactId,UUID userId)
    {
    	
    	return dbContacts.verifyOwner(contactId, userId) == 1;
    }
    
    
    public void deleteContact(UUID contactId) throws RuntimeException {
        System.out.println("🔴 Deleting contact: " + contactId);
        int rowsDeleted = dbContacts.deleteContactNative(contactId);
        System.out.println("🔴 Rows deleted: " + rowsDeleted);
    }
    
    
    
}
	

