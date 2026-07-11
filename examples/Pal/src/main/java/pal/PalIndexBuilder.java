package pal;

import fastairag.EmbeddingProvider;
import fastairag.FastAIRag;
import fastairag.RagDocument;
import fastairag.RagStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class PalIndexBuilder {

    public static void main(String[] args) {
        // Toggle ANSI/VT Mode in Windows console using FastTerminal native backend
        try {
            fastterminal.FastTerminal.setAnsiRawMode(true);
        } catch (Throwable ignored) {}

        System.out.println("==================================================");
        System.out.println("   Building Pal RAG Index (with FastTerminal)     ");
        System.out.println("==================================================");

        EmbeddingProvider dummyEmbedder = text -> {
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

        // Standard index file path inside project structure
        Path dbPath = Path.of("data/pal_index.db");
        System.out.println("Target index storage: " + dbPath.toAbsolutePath());

        try (RagStore store = FastAIRag.store(dummyEmbedder)) {
            Path snippetDir = Path.of("data/snippets");
            String[] files = {"cmd.txt", "git.txt"};
            for (String file : files) {
                Path path = snippetDir.resolve(file);
                if (path.toFile().exists()) {
                    System.out.println("Indexing: " + file);
                    List<String> snippets = SnippetLoader.loadSnippets(path);
                    int total = snippets.size();
                    for (int i = 0; i < total; i++) {
                        String text = snippets.get(i);
                        store.add(new RagDocument(
                            file + "-" + i,
                            text,
                            Map.of("source", file)
                        ));
                        
                        // Render progress bar (monochrome & full block chars █)
                        int percent = (int) (((double) (i + 1) / total) * 100);
                        int filled = percent / 4;
                        StringBuilder sb = new StringBuilder("\r[");
                        for (int j = 0; j < 25; j++) {
                            if (j < filled) sb.append("█");
                            else sb.append(" ");
                        }
                        sb.append("] ").append(percent).append("% (Chunk ").append(i + 1).append("/").append(total).append(")");
                        System.out.print(sb.toString());
                        try { Thread.sleep(20); } catch (InterruptedException ignored) {} // subtle progress feel
                    }
                    System.out.println();
                }
            }
            // Ensure data directory exists
            java.io.File parent = dbPath.toFile().getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            store.save(dbPath);
            System.out.println("\n[SUCCESS] Successfully generated index database!");
        } catch (Exception e) {
            System.err.println("\n[FAILED] Indexing failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
