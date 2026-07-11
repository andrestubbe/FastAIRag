package fastairag;

@FunctionalInterface
public interface EmbeddingProvider {
    float[] embed(String text);
}
