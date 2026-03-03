package com.springbootdeveloper.RepositoryLayer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import com.springbootdeveloper.Models.Contact;
import com.springbootdeveloper.Models.ContactGroup;

@Repository
public interface DatabaseLayerContacts extends CrudRepository<Contact , UUID> {	
	
	
	Optional<Contact> findByNameAndGroup(String name, ContactGroup group);

}
