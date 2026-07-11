package fastairag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class Chunker {

    private Chunker() {}

    public static List<String> chunk(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        // Simple token-like approximation by splitting on whitespace
        String[] words = text.split("\\s+");
        if (words.length <= chunkSize) {
            chunks.add(text);
            return chunks;
        }

        int step = chunkSize - overlap;
        if (step <= 0) {
            step = 1;
        }

        for (int i = 0; i < words.length; i += step) {
            int end = Math.min(i + chunkSize, words.length);
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < end; j++) {
                sb.append(words[j]).append(" ");
            }
            chunks.add(sb.toString().trim());
            if (end == words.length) {
                break;
            }
        }
        return chunks;
    }

    public static List<RagDocument> chunkDirectory(Path dir, int chunkSize, int overlap) throws IOException {
        List<RagDocument> documents = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".txt") || p.toString().endsWith(".md"))
                 .forEach(path -> {
                     try {
                         String content = Files.readString(path);
                         List<String> textChunks = chunk(content, chunkSize, overlap);
                         for (int i = 0; i < textChunks.size(); i++) {
                             String id = UUID.randomUUID().toString();
                             documents.add(new RagDocument(
                                 id,
                                 textChunks.get(i),
                                 Map.of("source", path.toAbsolutePath().toString(), "chunkIndex", i)
                             ));
                         }
                     } catch (IOException ignored) {}
                 });
        }
        return documents;
    }
}
