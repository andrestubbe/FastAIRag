package pal;

public final class PalPromptBuilder {

    private PalPromptBuilder() {}

    public static String buildSystemPrompt(String context) {
        return "You are Pal, a helpful local Windows command-line assistant. " +
               "Use the following snippets context to answer the user query accurately. " +
               "Always answer with the precise terminal commands formatted in clean code blocks, followed by a minimal explanation of what the command does.\n\n" +
               context;
    }
}
