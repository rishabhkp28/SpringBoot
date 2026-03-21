package com.springbootdeveloper.Models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(
	    name = "contacts",
	    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","group_name", "name","phone"})
	)
public class Contact {
//everything gets converted to snake case doesnt matter what ever we do or explicitly define the names 
    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID contactId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String nickName;

   

    @Column(length = 200)
    private String description;
    
    @Column(length = 10)
    private String phone;

    @Column(nullable = false,length = 300)
    private String email;
    
   @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = true) //this will store the UserId for hte User as a foreign key,
    private User user;//in hibernate we wouldnt be able to delete contact directly from user side if we didnt set this to true...(in dbms we could)(orphan removal = True)

    private String image;
    
    private boolean favourite;

    // ===== New fields =====
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, name = "group_name")
    private ContactGroup group;

	private LocalDateTime updatedDate;

	
    public UUID getContactId() { return contactId; }
    public void setContactId(UUID contactId) { this.contactId = contactId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }


    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }

    public ContactGroup getGroup() { return group; }
    public void setGroup(ContactGroup group) { this.group = group; }
    
    
    public void setUser(User user) {
    	this.user = user;
    }
    public User getUser()
    {
    	return user;
    }
    
    public void setFavourite(boolean favourite)
    {
    	this.favourite = favourite;
    }
    public boolean getFavourite() { return favourite;}
  	
	// Automatically set createdDate before saving
    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }
    @PreUpdate  // for the update date of the contact
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}