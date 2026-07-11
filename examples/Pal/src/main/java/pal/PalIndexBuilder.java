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
        System.out.println("Building Pal RAG Index...");

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
                    for (int i = 0; i < snippets.size(); i++) {
                        String text = snippets.get(i);
                        System.out.println("  -> Chunk " + i + " (" + text.split("\n")[0] + ")");
                        store.add(new RagDocument(
                            file + "-" + i,
                            text,
                            Map.of("source", file)
                        ));
                    }
                }
            }
            System.out.println("Successfully generated index database!");
        } catch (Exception e) {
            System.err.println("Indexing failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
