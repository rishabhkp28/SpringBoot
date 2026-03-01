package com.springbootdeveloper.Models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID contactId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String nickName;

    private String work;

    @Column(length = 500)
    private String description;

    private Long phone;

    @Column(nullable = false)
    private String email;

    private String image;

    // ===== New fields =====
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContactGroup group;

	private LocalDateTime updatedDate;

    // ===== getters & setters =====

    public UUID getContactId() { return contactId; }
    public void setContactId(UUID contactId) { this.contactId = contactId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getWork() { return work; }
    public void setWork(String work) { this.work = work; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getPhone() { return phone; }
    public void setPhone(Long phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }

    public ContactGroup getGroup() { return group; }
    public void setGroup(ContactGroup group) { this.group = group; }

    // Automatically set createdDate before saving
    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }
    @PreUpdate  // add this
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}