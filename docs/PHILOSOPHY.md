# FastAIRag Philosophy

FastAIRag is built around three core principles:

1. **Zero Context-Loss Retrieval**: Uses Parent-Child chunking (`FastContentChunk`) so small chunks drive vector search precision while large parent sections drive LLM prompt synthesis.
2. **Minimalist Architecture**: Zero heavy Python containers, zero Docker dependencies. Operates 100% within the JVM.
3. **Ecosystem Integration**: Seamlessly orchestrates `FastContentParse` (ingestion), `FastContentChunk` (splitting), `FastAIVectorDB` (vector store), and `FastAIBot` (generative AI).
