package haivo.chatbot.controller;

import haivo.chatbot.dto.BillItem;
import haivo.chatbot.dto.ChatRequest;
import haivo.chatbot.dto.ExpenseInfo;
import haivo.chatbot.dto.FilmInfo;
import haivo.chatbot.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ChatController {

    private final ChatService chatService;
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    @PostMapping("/chat")
    public ExpenseInfo chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }
    @PostMapping("/chat-with-image")
    public List<BillItem> chatWithImage(@RequestParam ("file") MultipartFile file,
                                        @RequestParam ("message") String message){
        return chatService.chatWithImage(file, message);
    }
}
