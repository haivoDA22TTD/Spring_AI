package haivo.chatbot.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.List;

@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    @Value("${rag.vector-store-path:./vector-store.json}")
    private String vectorStorePath;

    @Value("${rag.document-url}")
    private String documentUrl;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public String ingestFromUrl(String url) {
        try {
            UrlResource resource = new UrlResource(new URL(url));
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            List<Document> documents = reader.get();

            if (documents.isEmpty()) {
                return "Không tìm thấy nội dung từ URL: " + url;
            }

            // Giới hạn chunk size để tránh rate limit
            TokenTextSplitter splitter = new TokenTextSplitter(500, 100, 5, 1000, true);
            List<Document> chunks = splitter.apply(documents);

            // Giới hạn số chunks để tránh rate limit embedding
            int maxChunks = Math.min(chunks.size(), 50);
            List<Document> limitedChunks = chunks.subList(0, maxChunks);

            vectorStore.add(limitedChunks);
            saveVectorStore();

            return "Đã nạp " + limitedChunks.size() + "/" + chunks.size() + " phần tài liệu từ: " + url;
        } catch (Exception e) {
            return "Lỗi khi nạp tài liệu: " + e.getMessage();
        }
    }

    public String ingestDefaultDocuments() {
        return ingestFromUrl(documentUrl);
    }

    private void saveVectorStore() {
        if (vectorStore instanceof SimpleVectorStore simpleVectorStore) {
            simpleVectorStore.save(new File(vectorStorePath));
        }
    }
}
