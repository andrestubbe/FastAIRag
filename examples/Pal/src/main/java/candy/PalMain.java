package candy;

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
        System.out.println("Initializing Pal (Local CLI Assistant)...");

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
            // Load snippet docs
            Path snippetDir = Path.of("data/snippets");
            String[] files = {"cmd.txt", "powershell.txt", "bash.txt", "git.txt", "docker.txt"};
            for (String file : files) {
                Path path = snippetDir.resolve(file);
                if (path.toFile().exists()) {
                    List<String> snippets = SnippetLoader.loadSnippets(path);
                    for (int i = 0; i < snippets.size(); i++) {
                        store.add(new RagDocument(
                            file + "-" + i,
                            snippets.get(i),
                            Map.of("source", file)
                        ));
                    }
                }
            }
            System.out.println("Loaded " + files.length + " snippet references.");

            // Connect to LLM
            System.out.println("Connecting to local LLM...");
            AI ai = FastAI.connect("ollama:llama3.2"); // Default Ollama target

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

                System.out.println("Pal > thinking...");
                String answer = ai.ask(systemPrompt, input);
                System.out.println("\n" + answer);
            }

        } catch (Exception e) {
            System.err.println("Error initializing Pal: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
