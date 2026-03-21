package com.springbootdeveloper.Models;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 300)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    private String image;

    @Column(length = 300)
    private String bio;

    private boolean enable;

    private String role;

    @OneToMany(mappedBy ="user",  cascade = CascadeType.ALL , fetch = FetchType.EAGER , orphanRemoval = true)
    List<Contact> contacts = new ArrayList<>();

	public UUID getUserId() {
		return userId;
	}
	public void setUserId(UUID userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getBio() {
		return bio;
	}
	public void setBio(String bio) {
		this.bio = bio;
	}
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public List<Contact> getContacts() {
		return contacts;
	}
	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}
	public void addContact(Contact contact) {
	    this.contacts.add(contact);
	}
	public void removeContact(Contact contact) {
	    this.contacts.remove(contact);
	    contact.setUser(null);
	}
}