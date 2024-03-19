package com.template.letschat.RepoInterfaces;

import com.template.letschat.Domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {

    // Fetch all messages for a specific user
    List<ChatMessage> findBySenderUsername(String senderUsername);

    // Fetch all messages between two users
    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE (cm.senderUsername = :user1 AND cm.recipientUsername = :user2) OR " +
            "(cm.senderUsername = :user2 AND cm.recipientUsername = :user1) " +
            "ORDER BY cm.sentAt ASC")
    List<ChatMessage> findMessagesBetweenUsers(@Param("user1") String user1, @Param("user2") String user2);

    // Fetches all messages sent after a specific timestamp
    List<ChatMessage> findBySentAtAfter(LocalDateTime timestamp);

    // Fetches all messages for a user's active session
    @Query("SELECT cm FROM ChatMessage cm " +
            "WHERE cm.senderUsername = :username AND cm.sessionId = :sessionId " +
            "ORDER BY cm.sentAt ASC")
    List<ChatMessage> findMessagesForUserSession(@Param("username") String username, @Param("sessionId") String sessionId);

    // Updates the read status of a message for a recipient
    @Query("UPDATE ChatMessage cm SET cm.readByRecipient = true " +
            "WHERE cm.id = :messageId AND cm.recipientUsername = :recipientUsername")
    void markMessageAsReadByRecipient(@Param("messageId") Long messageId, @Param("recipientUsername") String recipientUsername);
}
