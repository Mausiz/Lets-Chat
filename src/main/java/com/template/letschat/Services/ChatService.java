package com.template.letschat.Services;

import org.springframework.stereotype.Service;

import com.template.letschat.Domain.ChatMessage;
import com.template.letschat.RepoInterfaces.ChatMessageRepo;

@Service
public class ChatService {
    private final ChatMessageRepo chatMessageRepository;

    public ChatService(ChatMessageRepo chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public void saveMessage(ChatMessage message) {
        chatMessageRepository.save(message);
    }

    // Someone else add methods to fetch messages, handle user sessions and other.
}
