package haivo.chatbot.service;

import haivo.chatbot.dto.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;

    public ChatService(ChatClient.Builder builder,  JdbcChatMemoryRepository jdbcChatMemoryRepository) {

        this.jdbcChatMemoryRepository = jdbcChatMemoryRepository;

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(30)
                .build();

        chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public String chat(ChatRequest request) {
        String conversationId = "006";

        SystemMessage systemMessage = new SystemMessage("""
                Hey there! I'm haivoDev, your friendly virtual assistant 🤖✨
                                        Here to help you with anything you need, just ask away!
                """);
        UserMessage userMessage = new UserMessage(request.message());
        Prompt prompt = new Prompt(systemMessage, userMessage);
        return chatClient
                .prompt(prompt)
                .advisors(advisorSpec -> advisorSpec.param(
                        ChatMemory.CONVERSATION_ID, conversationId
                ))
                .call()
                .content();
    }

    public String chatWithImage(MultipartFile file, @RequestParam String message) {
        Media media = Media.builder()
                .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                .data(file.getResource())
                .build();
        return chatClient.prompt()
                .system("Hey there! I'm haivoDev, your friendly virtual assistant 🤖✨")
                .user(promptUserSpec
                -> promptUserSpec.media(media)
                .text(message))
                .call()
                .content();
    }
}

