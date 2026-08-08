# FastAIRag 0.1.1 — Unified, Zero-Bloat RAG Pipeline Client for Java

[![Status](https://img.shields.io/badge/status-0.1.1-brightgreen.svg)](https://github.com/andrestubbe/FastAIRag/releases/tag/0.1.1)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.1-green.svg)](https://jitpack.io/#andrestubbe/FastAIRag)

---

**⚡ Connect local vector storage with generative AI models — Minimalist retrieval pipeline orchestrating chunking, embeddings, and context insertion.**

FastAIRag is a **lightweight, framework-agnostic RAG engine** designed to feed relevant context into local or cloud models with zero framework bloat. It is designed to work alongside **[FastContentParse](https://github.com/andrestubbe/FastContentParse)**, **[FastContentChunk](https://github.com/andrestubbe/FastContentChunk)**, and **[FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB)** to orchestrate text parsing, SIMD chunking, vector storage, and Parent-Child context retention.

[![Showcase](docs/screenshot.png)](https://youtu.be/4dDMeUfrQ3w)

---

## Quick Start — Example

```java
import fastai.AI;
import fastai.FastAI;
import fastairag.EmbeddingProvider;
import fastairag.FastAIRag;
import fastairag.RagPipeline;
import fastairag.RagStore;
import java.nio.file.Path;

public class Demo {
    public static void main(String[] args) throws Exception {
        // 1. Define Embedding Provider (e.g. Local Endpoint or Model API)
        EmbeddingProvider embedder = text -> new float[384]; 

        // 2. Instantiate Document Store and Index Documents
        RagStore store = FastAIRag.store(embedder);
        store.addDirectory(Path.of("./docs"), 512, 128);

        // 3. Attach LLM Engine & Query RAG Pipeline
        AI llm = FastAI.connect("ollama:llama3.2");
        RagPipeline pipeline = FastAIRag.pipeline(llm, store);

        String answer = pipeline.ask("How do I compile the native package?");
        System.out.println("AI Answer: " + answer);
    }
}
```

---

## Table of Contents

- [Why FastAIRag?](#why-fastairag)
- [Key Features](#key-features)
- [Performance Benchmarks](#performance-benchmarks)
- [Architecture Overview](#architecture-overview)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)
- [Related Projects](#related-projects)

---

## Why FastAIRag?

Traditional RAG frameworks force developers into heavyweight Python orchestration layers, LangChain abstractions, or complex REST setups. `FastAIRag` provides:

- **100% Native JVM Pipeline** — Orchestrates text parsing, tokenization, vector search, and LLM context insertion in a single JVM process.
- **Parent-Child Retrieval** — Eliminates context-loss by storing small `chunk.text` embeddings in `FastAIVectorDB` while inserting large `chunk.parentText` into the LLM system prompt.
- **Model Agnostic** — Works with local Ollama/LM Studio endpoints or cloud LLMs via the single-method `EmbeddingProvider`.
- **Zero Configuration Overlap** — Integrates seamlessly with `FastContentParse`, `FastContentChunk`, and `FastAIVectorDB`.

---

## Key Features

* **📂 Modular Chunking & Parsing** — Ingests entire document directories, parsing text via `FastContentParse` and chunking with `FastContentChunk`.
* **🧠 Parent-Child Context Retention** — Links small embeddings with large section context to prevent LLM hallucinations.
* **⚡ Integrated Vector Search** — Direct memory vector search powered by native `FastAIVectorDB`.
* **🔌 Model Agnostic** — Flexible `EmbeddingProvider` interface for any local or cloud embedding model.

---

## Performance Benchmarks

`FastAIRag` is built for high-throughput context retrieval and prompt construction. In the official [JMH Benchmark](examples/Benchmark), the end-to-end retrieval and context synthesis measured:

```text
Benchmark                                Mode  Cnt  Score   Error   Units
RagBenchmark.benchmarkContextRetrieval  thrpt    5  729.0   ± 203.0 ops/sec
```

> **729 Context Synthesis Queries per Second**: `FastAIRag` retrieves Top-K nearest vector hits and constructs full Parent-Child system prompts in **~1.3 milliseconds per query**.

---

## Architecture Overview

**[FastContentParse](https://github.com/andrestubbe/FastContentParse) (The Parser)**  
Converts unstructured binary documents (PDF, RTF, Markdown, TXT) into normalized UTF-8 text streams.

**[FastContentChunk](https://github.com/andrestubbe/FastContentChunk) (The Strategy Engine)**  
Segments normalized text streams into contextual passages with Parent-Child context.

**[FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB) (The Vector Store)**  
High-speed native C++ SIMD vector database storing small `chunk.text` embeddings for sub-5ms similarity retrieval.

**FastAIRag (This Library — The Orchestration Pipeline)**  
Higher-level RAG framework that orchestrates **[FastContentParse](https://github.com/andrestubbe/FastContentParse)** and **[FastContentChunk](https://github.com/andrestubbe/FastContentChunk)**, indexes small `chunk.text` embeddings into **[FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB)**, and feeds `chunk.parentText` to **[FastAIBot](https://github.com/andrestubbe/FastAIBot)** for LLM response generation.

---

## API Quick Reference

| Method | Description | Path |
|--------|-------------|------|
| `FastAIRag.store(EmbeddingProvider)` | Creates a new `RagStore` using the provided embedder. | [Reference →](docs/REFERENCE.md#fastairag) |
| `store.addDirectory(Path, int, int)` | Recursively parses and indexes document directory with chunking. | [Reference →](docs/REFERENCE.md#ragstore) |
| `FastAIRag.pipeline(AI, RagStore)` | Wraps LLM client and store into an end-to-end RAG pipeline. | [Reference →](docs/REFERENCE.md#fastairag) |
| `pipeline.ask(String)` | Searches context, constructs system prompt, and calls LLM. | [Reference →](docs/REFERENCE.md#ragpipeline) |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependency to your `pom.xml`:

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
        <version>0.1.1</version>
    </dependency>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAIVectorDB</artifactId>
        <version>0.1.0</version>
    </dependency>
    <!-- Required for native library loading -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastAIRag:0.1.1'
    implementation 'com.github.andrestubbe:FastAIVectorDB:0.1.1'
    // Required for native library loading
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 💡 **[FastAIRag-0.1.1.jar](https://github.com/andrestubbe/FastAIRag/releases/download/0.1.1/FastAIRag-0.1.1.jar)** (RAG Engine)
2. ⚡ **[FastAIVectorDB-0.1.1.jar](https://github.com/andrestubbe/FastAIVectorDB/releases/download/0.1.1/FastAIVectorDB-0.1.1.jar)** (The Vector Store)
3. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (Required Native JNI Loader)

> [!IMPORTANT]
> All JARs must be included in your classpath for the native JNI bindings to function correctly.

---

## Documentation

* **[REFERENCE.md](docs/REFERENCE.md)**: Core API reference manual.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Parent-Child RAG pipeline design goals.
* **[COMPILE.md](docs/COMPILE.md)**: Maven build instructions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Project history.
* **[ROADMAP.md](docs/ROADMAP.md)**: Future development goals.

---

## Platform Support

| Platform | Status |
|----------|--------|
| Windows 10/11 (x64) | ✅ Fully Supported |
| Linux | 🚧 Planned |
| macOS | 🚧 Planned |

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastContentParse](https://github.com/andrestubbe/FastContentParse) — Standardized Java document parser for text extraction and normalization
- [FastContentChunk](https://github.com/andrestubbe/FastContentChunk) — High-performance native SIMD tokenizer and multi-mode strategy chunker
- [FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB) — High-speed native C++ SIMD vector database
- [FastCore](https://github.com/andrestubbe/FastCore) — Native JNI loader for FastJava libraries
- [FastAI](https://github.com/andrestubbe/fastai) — Unified lightweight AI model client interface
- [FastAIModel](https://github.com/andrestubbe/FastAIModel) — Embedded GGUF and ONNX runtimes for local feature embeddings
- [FastAIBot](https://github.com/andrestubbe/FastAIBot) — Autonomous conversational AI bot engine
- [FastAIAgent](https://github.com/andrestubbe/FastAIAgent) — Autonomous agentic workflow execution framework

---

Part of the FastJava Ecosystem — Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋
