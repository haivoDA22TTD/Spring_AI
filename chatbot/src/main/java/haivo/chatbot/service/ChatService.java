package haivo.chatbot.service;

import haivo.chatbot.dto.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    private static final String SYSTEM_PROMPT = """
            Hey there! I'm haivoDev, your friendly virtual assistant 🤖✨
            Here to help you with anything you need, just ask away!
            """;

    private static final String RAG_SYSTEM_PROMPT = """
            Bạn là trợ lý ảo chuyên về Spring Boot. Hãy trả lời câu hỏi dựa trên context được cung cấp.
            Nếu không tìm thấy thông tin trong context, hãy trả lời dựa trên kiến thức của bạn.
            
            QUY TẮC TRẢ LỜI BẮT BUỘC:
            - Trả lời bằng tiếng Việt, rõ ràng và dễ hiểu
            - Sử dụng định dạng Markdown
            - Dùng **bold** cho từ khóa quan trọng
            - Dùng `code` cho tên class, method, annotation
            - Dùng ```java ... ``` cho code blocks
            - Sử dụng danh sách khi liệt kê
            
            QUAN TRỌNG - HÌNH ẢNH MINH HỌA:
            - LUÔN LUÔN thêm ít nhất 1 hình ảnh minh họa liên quan đến câu trả lời
            - Sử dụng hình ảnh từ các nguồn sau:
              + Spring Boot logo: ![Spring Boot](https://spring.io/img/projects/spring-boot.svg)
              + Spring Framework: ![Spring](https://spring.io/img/spring-logo.svg)
              + Java logo: ![Java](https://dev.java/assets/images/java-logo-vert-blk.png)
              + Architecture diagram: ![Architecture](https://miro.medium.com/v2/resize:fit:1400/1*vFiGOTV1S8yz0RTIQteTjw.png)
              + REST API: ![REST](https://miro.medium.com/v2/resize:fit:800/1*uHzooF1EtgcKn9_XiSST4w.png)
              + Database: ![Database](https://cdn-icons-png.flaticon.com/512/2906/2906274.png)
              + Security: ![Security](https://cdn-icons-png.flaticon.com/512/2913/2913133.png)
            - Đặt hình ảnh ở đầu hoặc cuối câu trả lời
            
            Context:
            {context}
            """;

    public ChatService(ChatClient.Builder clientBuilder, VectorStore vectorStore) {
        this.chatClient = clientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public String chat(ChatRequest request) {
        try {
            SystemMessage systemMessage = new SystemMessage(SYSTEM_PROMPT);
            UserMessage userMessage = new UserMessage(request.message());
            Prompt prompt = new Prompt(systemMessage, userMessage);
            return retryWithBackoff(() -> chatClient
                    .prompt(prompt)
                    .call()
                    .content());
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }

    public String chatWithRag(ChatRequest request) {
        try {
            String context = "";
            try {
                List<Document> relevantDocs = vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(request.message())
                                .topK(3)
                                .build()
                );
                context = relevantDocs.stream()
                        .map(Document::getText)
                        .collect(Collectors.joining("\n\n"));
            } catch (Exception e) {
                context = "Chưa có tài liệu. Vui lòng nạp tài liệu trước.";
            }

            String systemPrompt = RAG_SYSTEM_PROMPT.replace("{context}", 
                    context.isEmpty() ? "Chưa có tài liệu được nạp." : context);

            return retryWithBackoff(() -> chatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(request.message())
                    .call()
                    .content());
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }

    public String chatWithImage(MultipartFile file, String message) {
        try {
            Media media = Media.builder()
                    .mimeType(MimeTypeUtils.parseMimeType(file.getContentType()))
                    .data(file.getResource())
                    .build();
            return retryWithBackoff(() -> chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(promptUserSpec -> promptUserSpec.media(media).text(message))
                    .call()
                    .content());
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }

    /**
     * Retry với exponential backoff để xử lý rate limit (429)
     */
    private <T> T retryWithBackoff(Supplier<T> action) {
        int maxRetries = 3;
        long waitTime = 2000; // 2 giây

        for (int i = 0; i < maxRetries; i++) {
            try {
                return action.get();
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("429") || msg.contains("rate") || msg.contains("quota")) {
                    if (i < maxRetries - 1) {
                        try {
                            Thread.sleep(waitTime);
                            waitTime *= 2; // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw e;
                        }
                    } else {
                        throw new RuntimeException("API rate limit. Vui lòng đợi 1-2 phút rồi thử lại.");
                    }
                } else {
                    throw e;
                }
            }
        }
        throw new RuntimeException("Không thể kết nối API");
    }
}
