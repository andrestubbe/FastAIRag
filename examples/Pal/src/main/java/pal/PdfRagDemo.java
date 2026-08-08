package pal;

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

public final class PdfRagDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== FastAIRag + FastContentParse + FastContentChunk Pipeline Demo ===\n");

        Path pdfPath = Path.of("..", "..", "FastContentChunk", "docs", "sample.pdf");
        if (!pdfPath.toFile().exists()) {
            pdfPath = Path.of("..", "..", "..", "FastContentChunk", "docs", "sample.pdf");
        }
        System.out.println("1. Extracting PDF text with FastContentParse from: " + pdfPath.toAbsolutePath());
        FastContentParse parser = new FastContentParse();
        String pdfText = parser.parseFile(pdfPath).getText();
        System.out.println("   Extracted " + pdfText.length() + " characters.\n");

        System.out.println("2. Intelligent Chunking with FastContentChunk (Mode: RECURSIVE)...");
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
                // Vector search uses c.text (small chunk), prompt context receives c.parentText
                RagDocument doc = new RagDocument(
                    "chunk-" + c.id,
                    c.text,
                    c.parentText,
                    Map.of("source", pdfPath.getFileName().toString(), "chunkId", c.id, "tokenCount", c.tokenCount)
                );
                store.add(doc);
                System.out.println("   Indexed Chunk #" + c.id + " [" + c.tokenCount + " tokens] -> Parent Context (" + c.parentText.length() + " chars)");
            }

            System.out.println("\n4. Executing RAG Search Query: 'What is the memory footprint and latency?'...");
            String query = "What is the memory footprint and latency?";
            String promptContext = store.buildContext(query, 2);

            System.out.println("\n========================================================");
            System.out.println("GENERATED RAG PROMPT CONTEXT (Sent to FastAIBot / LLM):");
            System.out.println("========================================================");
            System.out.println(promptContext);
        }
    }
}
