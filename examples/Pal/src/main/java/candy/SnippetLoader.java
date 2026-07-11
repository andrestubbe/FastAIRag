package candy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SnippetLoader {

    private SnippetLoader() {}

    public static List<String> loadSnippets(Path file) throws IOException {
        List<String> snippets = new ArrayList<>();
        if (!Files.exists(file)) {
            return snippets;
        }

        String content = Files.readString(file);
        // Split by double newline to segment different commands/sections
        String[] parts = content.split("(?m)^\\s*$");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                snippets.add(trimmed);
            }
        }
        return snippets;
    }
}
