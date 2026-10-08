package com.studymind.repository;

import com.studymind.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for Conversation entity.
 *
 * OOP / Viva Concepts:
 * - Interface Inheritance: ConversationRepository extends JpaRepository<Conversation, Long>.
 * - Spring Data JPA provides full CRUD (Save, FindById, Delete, FindAll) without writing SQL!
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findAllByOrderByCreatedAtDesc();
}
