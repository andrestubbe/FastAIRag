# FastAIRag Reference Docs

## Classes

### `EmbeddingProvider`
Functional interface to map string queries into embeddings.
```java
@FunctionalInterface
public interface EmbeddingProvider {
    float[] embed(String text);
}
```

### `RagStore`
Manages token parsing, directory chunking, embedding acquisition, and storage.
```java
public final class RagStore implements AutoCloseable {
    public RagStore(EmbeddingProvider embeddingProvider);
    public void add(RagDocument doc);
    public void addDirectory(Path dir, int chunkSize, int overlap) throws IOException;
    public List<RagDocument> search(String query, int topK);
    public String buildContext(String query, int topK);
}
```
