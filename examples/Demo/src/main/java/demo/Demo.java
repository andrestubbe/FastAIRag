package demo;

import fastairag.EmbeddingProvider;
import fastairag.FastAIRag;
import fastairag.RagDocument;
import fastairag.RagStore;
import fastcontentchunk.Chunk;
import fastcontentchunk.ChunkConfig;
import fastcontentchunk.ChunkMode;
import fastcontentchunk.FastContentChunk;
import fastcontentparse.FastContentParse;

import java.nio.file.Path;
import java.util.Map;

public class Demo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== FastAIRag End-to-End Pipeline Demo ===");

        Path samplePath = Path.of("..", "..", "..", "FastContentParse", "docs", "sample.pdf");
        if (!samplePath.toFile().exists()) {
            samplePath = Path.of("..", "..", "docs", "sample.pdf");
        }
        System.out.println("1. Extracting PDF text with FastContentParse: " + samplePath.toAbsolutePath());
        FastContentParse parser = new FastContentParse();
        String pdfText = parser.parseFile(samplePath).getText();
        System.out.println("   Extracted " + pdfText.length() + " characters.\n");

        System.out.println("2. Chunking with FastContentChunk (Mode: RECURSIVE)...");
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(80, 15, ChunkMode.RECURSIVE);
        Chunk[] chunks = chunker.chunk(pdfText, config);
        System.out.println("   Generated " + chunks.length + " hierarchical chunks with Parent-Child context.\n");

        // Simple deterministic embedding provider for demo
        EmbeddingProvider embedder = text -> {
            float[] vec = new float[128];
            if (text == null) return vec;
            int hash = text.hashCode();
            for (int i = 0; i < vec.length; i++) {
                vec[i] = (float) Math.sin(hash + i);
            }
            float norm = 0f;
            for (float v : vec) norm += v * v;
            norm = (float) Math.sqrt(norm);
            if (norm != 0f) {
                for (int i = 0; i < vec.length; i++) vec[i] /= norm;
            }
            return vec;
        };

        System.out.println("3. Indexing chunks into FastAIRag RagStore...");
        try (RagStore store = FastAIRag.store(embedder)) {
            for (Chunk c : chunks) {
                RagDocument doc = new RagDocument(
                    "chunk-" + c.id,
                    c.text,
                    c.parentText,
                    Map.of("chunkId", c.id, "tokenCount", c.tokenCount)
                );
                store.add(doc);
                System.out.println("   Indexed Chunk #" + c.id + " [" + c.tokenCount + " tokens] -> Parent Context (" + c.parentText.length() + " chars)");
            }

            System.out.println("\n4. Querying RAG Context for LLM System Prompt...");
            String query = "What is the memory footprint and latency?";
            String promptContext = store.buildContext(query, 2);

            System.out.println("\nGenerated LLM Prompt Context:\n--------------------------------------------------------");
            System.out.println(promptContext);
            System.out.println("--------------------------------------------------------");
        }

        System.out.println("\n=== FastAIRag Demo Complete ===");
    }
}
