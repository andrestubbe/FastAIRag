package pal;

public final class PalPromptBuilder {

    private PalPromptBuilder() {}

    public static String buildSystemPrompt(String context) {
        return "You are Pal, a helpful local Windows command-line assistant. " +
               "Use the following snippets context to answer the user query accurately. " +
               "Always answer with the precise terminal commands in plain text (do NOT wrap them in markdown code blocks like ```cmd or ```). Follow the commands with a minimal explanation.\n\n" +
               context;
    }
}
