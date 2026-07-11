# FastAIRag 0.1.0 — Unified, Zero-Bloat RAG Pipeline Client for Java

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

---

**💡 Connect local vector storage with generative AI models — Minimalist retrieval pipeline orchestrating chunking, embeddings, and context insertion.**

FastAIRag is a **lightweight, framework-agnostic RAG engine** designed to feed relevant context into local or cloud models with zero framework bloat. It orchestrates file chunking, embedding generation, index mapping, and query formatting, giving local models (like 1B-3B LLMs) the domain knowledge they need to avoid hallucinations.

---

## Key Features

- **📂 Modular Chunking** — Ingest entire file systems or documents, parsing them into uniform sliding chunks.
- **🔌 Model Agnostic** — Easily integrate local embedders (like LM Studio or Ollama) using the single-method `EmbeddingProvider`.
- **⚡ Integrated Search** — Automatic routing and retrieval using the high-performance `FastAIVectorDB` engine.
- **🤖 Direct Pipe Integration** — Seamlessly feed retrieved context as system prompts directly into your `FastAI` connection.

---

## Quick Start

```java
import fastai.AI;
import fastai.FastAI;
import fastairag.*;

// 1. Define how your app obtains embeddings
EmbeddingProvider embedder = text -> myEmbeddingAPI(text);

// 2. Build the Document Store and index files
RagStore store = FastAIRag.store(embedder);
store.addDirectory(Path.of("./docs"), 512, 128);

// 3. Attach AI to generate context-supported answers
AI llm = FastAI.connect("ollama:llama3.2");
RagPipeline pipeline = FastAIRag.pipeline(llm, store);

String response = pipeline.ask("How do I compile the native package?");
System.out.println(response);
```

---

## Installation

### Maven (JitPack)
Add JitPack repository and dependency configuration to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAIRag</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAIVectorDB</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

---

## Technical Architecture

```
Offline Path:
[Raw Docs] ---> [Chunker] ---> [EmbeddingProvider] ---> [FastVectorDB]

Online Path:
[User Query] ---> [EmbeddingProvider] ---> [FastVectorDB Query] ---> [Context]
                                                                        |
                                                                        v
[Response] <--- [FastAI LLM Inference] <--- [Injected System Prompt] <--+
```

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*
