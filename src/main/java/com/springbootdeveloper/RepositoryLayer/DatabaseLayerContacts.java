package com.springbootdeveloper.RepositoryLayer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import com.springbootdeveloper.Models.Contact;
import com.springbootdeveloper.Models.ContactGroup;
import com.springbootdeveloper.Models.User;

import java.util.List;


@Repository
public interface DatabaseLayerContacts extends CrudRepository<Contact , UUID> {	
	
	
	Optional<Contact> findByNameAndGroup(String name, ContactGroup group);
	
	List<Contact> findTop10ByUser_UserIdOrderByUpdatedDateDesc(UUID userId);////hibernate first checks for entity if we pass entity here 
	//but passing the uuid (its a value) hibernate doesnt look for entity first in the session
	/*You call new User() via convertToEntity() → Hibernate never touched this object
	You pass it to repo → Hibernate says "who is this?" → problem Bottomline- It keeps the track of the entity*/
	
	/* name Spring Data goes inside User and finds userId. So the full path is contact → users → userId.*/

}
