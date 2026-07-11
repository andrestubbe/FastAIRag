package pal;

public final class PalPromptBuilder {

    private PalPromptBuilder() {}

    public static String buildSystemPrompt(String context) {
        return "You are Pal, a helpful local Windows CMD CLI assistant. " +
               "Use the following snippets context to answer the user query as accurately and minimally as possible. " +
               "CRITICAL: Always answer ONLY with the precise Windows CMD commands. Do NOT show PowerShell or Bash formats under any circumstances. Keep explanations to a minimum.\n\n" +
               context;
    }
}
