package com.springbootdeveloper.RepositoryLayer;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import com.springbootdeveloper.Models.Contact;
import com.springbootdeveloper.Models.ContactGroup;
import com.springbootdeveloper.Models.User;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface DatabaseLayerContacts extends CrudRepository<Contact , UUID> {	
	
	
	Optional<Contact> findByNameAndGroup(String name, ContactGroup group);
	
	List<Contact> findTop10ByUser_UserIdOrderByUpdatedDateDesc(UUID userId);////hibernate first checks for entity if we pass entity here 
	//but passing the uuid (its a value) hibernate doesnt look for entity first in the session
	/*You call new User() via convertToEntity() → Hibernate never touched this object
	You pass it to repo → Hibernate says "who is this?" → problem Bottomline- It keeps the track of the entity*/
	
	/* name Spring Data goes inside User and finds userId. So the full path is contact → users → userId.*/

	
	List<Contact> findByUser_UserId(UUID id);
	long countByUser_UserId(UUID id);
	
	@Query(value = "SELECT * FROM contacts WHERE user_id = :id AND favourite = true", nativeQuery = true)//exploring alternatives
	List<Contact> findFavouriteContacts(UUID id);	
	
	@Query(value = "SELECT count(*) FROM contacts WHERE user_id  = :id AND favourite = true", nativeQuery = true)
	long findFavouriteCount(UUID id);
	
	
	List<Contact> findByUser_UserIdAndCreatedDateBetween(UUID userId,
            LocalDateTime start,
            LocalDateTime end);
	
	
	long countByUser_UserIdAndCreatedDateBetween(UUID userId,
            LocalDateTime start,
            LocalDateTime end);
	
	

		List<Contact> findByUser_UserIdAndGroup(UUID userId, ContactGroup group);
		
		long countByUser_UserIdAndGroup(UUID userId, ContactGroup group);


	
	

		@Query(value = """
			    SELECT EXISTS (
			        SELECT 1 
			        FROM contacts 
			        WHERE contact_id = :contactId 
			        AND user_id = :userId
			    )
			""", nativeQuery = true)
			Integer verifyOwner(UUID contactId, UUID userId);
		
		
		
		@Modifying(clearAutomatically = true)  // (Defines whether we should clear the underlying persistence context after executing the modifying query.
		@Query(value = "DELETE FROM contacts WHERE contact_id = :contactId", nativeQuery = true)
	    int deleteContactNative(@Param("contactId") UUID contactId);
	    

}

