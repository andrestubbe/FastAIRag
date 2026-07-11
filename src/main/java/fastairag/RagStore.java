package fastairag;

import fastaivectordb.FastVectorDB;
import fastaivectordb.SearchResult;
import fastaivectordb.VectorEntry;

import java.io.IOException;
import java.nio.file.Files;
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

    public void save(Path path) {
        vectorDb.save(path.toAbsolutePath().toString());
        // Save documentsMap metadata associated with IDs to a sibling properties/data file
        Path metaPath = Path.of(path.toAbsolutePath().toString() + ".ragmeta");
        try (java.io.DataOutputStream out = new java.io.DataOutputStream(new java.io.FileOutputStream(metaPath.toFile()))) {
            out.writeInt(documentsMap.size());
            for (Map.Entry<Integer, RagDocument> entry : documentsMap.entrySet()) {
                out.writeInt(entry.getKey());
                out.writeUTF(entry.getValue().id());
                out.writeUTF(entry.getValue().text());
                out.writeInt(entry.getValue().metadata().size());
                for (Map.Entry<String, Object> meta : entry.getValue().metadata().entrySet()) {
                    out.writeUTF(meta.getKey());
                    out.writeUTF(String.valueOf(meta.getValue()));
                }
            }
        } catch (java.io.IOException ex) {
            throw new RuntimeException("Failed to serialize RAG metadata: " + ex.getMessage(), ex);
        }
    }

    public void load(Path path) {
        if (!Files.exists(path)) return;
        vectorDb.load(path.toAbsolutePath().toString());
        Path metaPath = Path.of(path.toAbsolutePath().toString() + ".ragmeta");
        if (Files.exists(metaPath)) {
            try (java.io.DataInputStream in = new java.io.DataInputStream(new java.io.FileInputStream(metaPath.toFile()))) {
                documentsMap.clear();
                int size = in.readInt();
                int maxId = 0;
                for (int i = 0; i < size; i++) {
                    int key = in.readInt();
                    if (key > maxId) maxId = key;
                    String docId = in.readUTF();
                    String text = in.readUTF();
                    int metaSize = in.readInt();
                    java.util.Map<String, Object> metaMap = new java.util.HashMap<>();
                    for (int j = 0; j < metaSize; j++) {
                        metaMap.put(in.readUTF(), in.readUTF());
                    }
                    documentsMap.put(key, new RagDocument(docId, text, metaMap));
                }
                idCounter.set(maxId + 1);
            } catch (java.io.IOException ex) {
                throw new RuntimeException("Failed to deserialize RAG metadata: " + ex.getMessage(), ex);
            }
        }
    }

    @Override
    public void close() {
        vectorDb.close();
    }
}
