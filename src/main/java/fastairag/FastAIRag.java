package fastairag;

import fastai.AI;

public final class FastAIRag {

    private FastAIRag() {}

    public static RagPipeline pipeline(AI ai, RagStore store) {
        return new RagPipeline(ai, store);
    }

    public static RagStore store(EmbeddingProvider embedder) {
        return new RagStore(embedder);
    }
}
