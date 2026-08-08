package fastairag;

import java.util.Map;

public record RagDocument(String id, String text, String parentText, Map<String, Object> metadata) {
    public RagDocument(String id, String text, Map<String, Object> metadata) {
        this(id, text, text, metadata);
    }
}
