# FastAIRag Reference Manual

## Core API

### `FastAIRag`
Factory entry point to instantiate RAG stores and orchestration pipelines.

```java
// 1. Create a RagStore with custom embedding provider
RagStore store = FastAIRag.store(embeddingProvider);

// 2. Index files or directories with parent-child chunking
store.addDirectory(Path.of("./docs"), 512, 64);

// 3. Attach LLM to execute RAG queries
RagPipeline pipeline = FastAIRag.pipeline(llmClient, store);
String answer = pipeline.ask("Your question here");
```

### `RagStore`
Maintains document embeddings, chunk mappings, and Parent-Child context references.
- `addDocument(String id, String text)`
- `addDirectory(Path path, int maxTokens, int overlap)`
- `search(String query, int topK)`

### `RagPipeline`
High-level prompt constructor injecting retrieved `chunk.text` or `chunk.parentText` passages into LLM prompts.
