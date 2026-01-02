package haivo.chatbot.controller;

import haivo.chatbot.dto.ChatRequest;
import haivo.chatbot.service.ChatService;
import haivo.chatbot.service.DocumentIngestionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ChatController {

    private final ChatService chatService;
    private final DocumentIngestionService documentIngestionService;

    public ChatController(ChatService chatService, DocumentIngestionService documentIngestionService) {
        this.chatService = chatService;
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }

    @PostMapping("/chat/rag")
    public String chatWithRag(@RequestBody ChatRequest request) {
        return chatService.chatWithRag(request);
    }

    @PostMapping("/chat-with-image")
    public String chatWithImage(@RequestParam("file") MultipartFile file,
                                @RequestParam("message") String message) {
        return chatService.chatWithImage(file, message);
    }

    @PostMapping("/documents/ingest")
    public String ingestDocuments(@RequestParam(value = "url", required = false) String url) {
        if (url != null && !url.isEmpty()) {
            return documentIngestionService.ingestFromUrl(url);
        }
        return documentIngestionService.ingestDefaultDocuments();
    }
}
