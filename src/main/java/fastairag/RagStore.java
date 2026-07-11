package fastairag;

import fastaivectordb.FastVectorDB;
import fastaivectordb.SearchResult;
import fastaivectordb.VectorEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class RagStore implements AutoCloseable {

    private final FastVectorDB vectorDb;
    private final EmbeddingProvider embeddingProvider;
    private final Map<Integer, RagDocument> documentsMap = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public RagStore(EmbeddingProvider embeddingProvider) {
        this.vectorDb = new FastVectorDB();
        this.embeddingProvider = embeddingProvider;
    }

    public void add(RagDocument doc) {
        float[] vector = embeddingProvider.embed(doc.text());
        int numericId = idCounter.getAndIncrement();
        documentsMap.put(numericId, doc);
        vectorDb.insert(new VectorEntry(numericId, vector, doc.text()));
    }

    public void addDirectory(Path dir, int chunkSize, int overlap) throws IOException {
        List<RagDocument> docs = Chunker.chunkDirectory(dir, chunkSize, overlap);
        for (RagDocument doc : docs) {
            add(doc);
        }
    }

    public List<RagDocument> search(String query, int topK) {
        float[] qvec = embeddingProvider.embed(query);
        List<SearchResult> results = vectorDb.search(qvec, topK);
        List<RagDocument> docs = new ArrayList<>(results.size());
        for (SearchResult result : results) {
            RagDocument doc = documentsMap.get(result.entry().id());
            if (doc != null) {
                docs.add(doc);
            }
        }
        return docs;
    }

    public String buildContext(String query, int topK) {
        List<RagDocument> hits = search(query, topK);
        StringBuilder sb = new StringBuilder();
        sb.append("Context information:\n---\n");
        for (RagDocument hit : hits) {
            sb.append("Source: ").append(hit.metadata().getOrDefault("source", "Unknown")).append("\n");
            sb.append(hit.text()).append("\n---\n");
        }
        return sb.toString();
    }

    @Override
    public void close() {
        vectorDb.close();
    }
}
