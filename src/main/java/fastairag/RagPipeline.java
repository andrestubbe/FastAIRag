package fastairag;

import fastai.AI;
import fastai.AIRequest;

public final class RagPipeline {

    private final AI ai;
    private final RagStore store;

    public RagPipeline(AI ai, RagStore store) {
        this.ai = ai;
        this.store = store;
    }

    public String ask(String question) {
        return ask(question, 3);
    }

    public String ask(String question, int topK) {
        String context = store.buildContext(question, topK);
        String systemPrompt = "You are a helpful assistant. Use the following context to answer the user query as accurately as possible:\n\n" + context;
        return ai.ask(systemPrompt, question);
    }
}
