# FastAIRag

Unified Retrieval-Augmented Generation library for the FastJava AI Ecosystem.

```java
RagStore store = FastAIRag.store(text -> getEmbedding(text));
store.addDirectory(Path.of("./docs"), 512, 128);

RagPipeline pipeline = FastAIRag.pipeline(ai, store);
System.out.println(pipeline.ask("How to run the compiler?"));
```
