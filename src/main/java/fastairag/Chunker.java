package fastairag;

import fastcontentchunk.Chunk;
import fastcontentchunk.ChunkConfig;
import fastcontentchunk.ChunkMode;
import fastcontentchunk.FastContentChunk;

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
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(chunkSize, overlap, ChunkMode.RECURSIVE);
        Chunk[] chunks = chunker.chunk(text, config);

        List<String> result = new ArrayList<>(chunks.length);
        for (Chunk c : chunks) {
            result.add(c.text);
        }
        return result;
    }

    public static List<RagDocument> chunkDirectory(Path dir, int chunkSize, int overlap) throws IOException {
        List<RagDocument> documents = new ArrayList<>();
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(chunkSize, overlap, ChunkMode.RECURSIVE);

        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".txt") || p.toString().endsWith(".md"))
                 .forEach(path -> {
                     try {
                         String content = Files.readString(path);
                         Chunk[] chunks = chunker.chunk(content, config);
                         for (int i = 0; i < chunks.length; i++) {
                             String id = UUID.randomUUID().toString();
                             Chunk c = chunks[i];
                             documents.add(new RagDocument(
                                 id,
                                 c.text,
                                 c.parentText,
                                 Map.of("source", path.toAbsolutePath().toString(), "chunkIndex", i, "startOffset", c.startCharOffset, "endOffset", c.endCharOffset)
                             ));
                         }
                     } catch (IOException ignored) {}
                 });
        }
        return documents;
    }
}
