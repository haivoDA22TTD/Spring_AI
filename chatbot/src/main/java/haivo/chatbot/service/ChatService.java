package haivo.chatbot.service;

import haivo.chatbot.dto.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient chatClient;
    public ChatService(ChatClient.Builder clientBuilder) {
        chatClient = clientBuilder.build();
    }

    public String chat(ChatRequest request) {
       return chatClient
               .prompt(request.message())
               .call()
               .content();
    }
}
