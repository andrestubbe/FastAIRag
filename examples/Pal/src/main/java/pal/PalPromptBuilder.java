package pal;

public final class PalPromptBuilder {

    private PalPromptBuilder() {}

    public static String buildSystemPrompt(String context) {
        return "You are Pal, a helpful local CLI command-line assistant. " +
               "Use the following snippets context to translate, correct, format, or generate precise commands. " +
               "Always answer with the precise terminal commands in code blocks or plain lines, showing CMD, PowerShell, and Bash formats where applicable.\n\n" +
               context;
    }
}
