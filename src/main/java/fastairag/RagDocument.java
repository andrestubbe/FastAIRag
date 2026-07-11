package fastairag;

import java.util.Map;

public record RagDocument(String id, String text, Map<String, Object> metadata) {
}
