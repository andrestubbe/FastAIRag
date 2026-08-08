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
import fastcontentparse.ParsedDocument;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * PalIndexBuilder — loads the existing snippet files (cmd.txt, git.txt) from
 * data/snippets/, parses each with FastContentParse, then splits into
 * Parent-Child chunks with FastContentChunk (RECURSIVE mode), and stores
 * the result in data/pal_index.db for PalMain to query.
 */
public final class PalIndexBuilder {

    public static void main(String[] args) {
        try {
            fastterminal.FastTerminal.setAnsiRawMode(true);
        } catch (Throwable ignored) {}

        System.out.println("=== Pal Index Builder ===");

        Path snippetDir = Path.of("data", "snippets");
        String[] files = {"cmd.txt", "git.txt"};

        if (!snippetDir.toFile().exists()) {
            System.out.println("[ERROR] Snippet directory not found: " + snippetDir.toAbsolutePath());
            return;
        }

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

        FastContentParse parser = new FastContentParse();
        FastContentChunk chunker = new FastContentChunk();
        ChunkConfig config = new ChunkConfig(128, 20, ChunkMode.RECURSIVE);

        Path dbPath = Path.of("data", "pal_index.db");

        try (RagStore store = FastAIRag.store(embedder)) {
            int totalChunks = 0;

            for (String file : files) {
                Path path = snippetDir.resolve(file);
                if (!path.toFile().exists()) {
                    System.out.println("  [SKIP] Not found: " + path);
                    continue;
                }

                System.out.println("Parsing  : " + file);
                try {
                    ParsedDocument doc = parser.parseFile(path);
                    String text = doc.getText();
                    if (text == null || text.isBlank()) {
                        System.out.println("  [SKIP] Empty document.");
                        continue;
                    }

                    Chunk[] chunks = chunker.chunk(text, config);
                    System.out.println("  Chunked : " + chunks.length + " chunks (RECURSIVE, maxTokens=128, overlap=20)");

                    int fileChunks = chunks.length;
                    for (int i = 0; i < fileChunks; i++) {
                        Chunk c = chunks[i];
                        store.add(new RagDocument(
                            file + "-" + c.id,
                            c.text,
                            c.parentText,
                            Map.of(
                                "source", file,
                                "chunkId", c.id,
                                "tokenCount", c.tokenCount
                            )
                        ));

                        // Progress bar
                        int percent = (int) (((double) (i + 1) / fileChunks) * 100);
                        int filled = percent / 4;
                        StringBuilder sb = new StringBuilder("\r  Indexing: [");
                        for (int j = 0; j < 25; j++) sb.append(j < filled ? "█" : " ");
                        sb.append("] ").append(percent).append("% (").append(i + 1).append("/").append(fileChunks).append(")");
                        System.out.print(sb);
                        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
                    }
                    System.out.println();
                    totalChunks += fileChunks;

                } catch (Exception e) {
                    System.out.println("  [FAILED] " + e.getMessage());
                }
            }

            // Ensure data directory exists and save index
            File parent = dbPath.toFile().getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            store.save(dbPath);
            System.out.println("\n[SUCCESS] Indexed " + totalChunks + " chunks from " + files.length + " file(s).");
            System.out.println("[SUCCESS] Index saved to: " + dbPath.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("\n[FAILED] Indexing failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
