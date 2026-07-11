package pal;

import fastai.AI;
import fastai.FastAI;
import fastairag.EmbeddingProvider;
import fastairag.FastAIRag;
import fastairag.RagDocument;
import fastairag.RagStore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class PalMain {

    public static void main(String[] args) {
        boolean directMode = (args != null && args.length > 0);
        if (!directMode) {
            System.out.println("Initializing Pal (Local CLI Assistant)...");
        }

        // Dummy embedding provider (maps words to simple normalized floats for demo purposes, 
        // fallback in case no local embedding model is running)
        EmbeddingProvider dummyEmbedder = text -> {
            float[] vec = new float[128];
            if (text == null) return vec;
            int hash = text.hashCode();
            for (int i = 0; i < vec.length; i++) {
                vec[i] = (float) Math.sin(hash + i);
            }
            // Normalize
            float norm = 0f;
            for (float v : vec) norm += v * v;
            norm = (float) Math.sqrt(norm);
            if (norm != 0f) {
                for (int i = 0; i < vec.length; i++) vec[i] /= norm;
            }
            return vec;
        };

        try (RagStore store = FastAIRag.store(dummyEmbedder)) {
            Path dbPath = Path.of("data/pal_index.db");
            if (dbPath.toFile().exists()) {
                store.load(dbPath);
                if (!directMode) {
                    System.out.println("Loaded index database from: " + dbPath.toAbsolutePath());
                }
            } else {
                System.out.println("Index database not found! Please build it first using run-build.bat.");
                return;
            }

            // Check if arguments are passed directly
            if (args != null && args.length > 0) {
                String input = String.join(" ", args);
                String context = store.buildContext(input, 3);
                String systemPrompt = PalPromptBuilder.buildSystemPrompt(context);
                AI ai = FastAI.connect("ollama:llama3.2:3b");
                System.out.println();
                ai.stream(systemPrompt, input, token -> {
                    System.out.print(token);
                    System.out.flush();
                });
                System.out.println();
                return;
            }

            // Connect to LLM
            System.out.println("Connecting to local LLM...");
            AI ai = FastAI.connect("ollama:llama3.2:3b"); // Default Ollama target

            System.out.println("\nPal is ready! Type your question (e.g. 'how to delete a directory') or 'exit' to quit.");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                System.out.print("\nYou > ");
                String input = reader.readLine();
                if (input == null || input.equalsIgnoreCase("exit")) {
                    break;
                }

                String context = store.buildContext(input, 3);
                String systemPrompt = PalPromptBuilder.buildSystemPrompt(context);

                System.out.println("Pal > ");
                ai.stream(systemPrompt, input, token -> {
                    System.out.print(token);
                    System.out.flush();
                });
                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("Error initializing Pal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
