package com.studymind.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Conversation entity representing a student's study session.
 *
 * OOP / Viva Concepts:
 * - Entity Encapsulation: State is protected via private fields and accessed via getters/setters.
 * - Composition (One-to-Many Relationship): A Conversation "has-a" collection of Messages.
 * - CascadeType.ALL: Operations (persist, remove) on a Conversation cascade down to its child messages.
 */
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    @JsonIgnore
    private List<ChatMessage> messages = new ArrayList<>();

    public Conversation() {
        this.createdAt = LocalDateTime.now();
    }

    public Conversation(String title) {
        this();
        this.title = title;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setConversation(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }
}
