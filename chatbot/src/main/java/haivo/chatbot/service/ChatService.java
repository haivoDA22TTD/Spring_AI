package haivo.chatbot.service;

import haivo.chatbot.dto.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
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

    public ChatService(ChatClient.Builder clientBuilder) {
        chatClient = clientBuilder.build();
    }

    public String chat(ChatRequest request) {
        SystemMessage systemMessage = new SystemMessage("""
                Hey there! I'm haivoDev, your friendly virtual assistant 🤖✨
                                        Here to help you with anything you need, just ask away!
                """);
        UserMessage userMessage = new UserMessage(request.message());
        Prompt prompt = new Prompt(systemMessage, userMessage);
        return chatClient
                .prompt(prompt)
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

