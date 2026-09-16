package fastairag.benchmark;

import fastairag.EmbeddingProvider;
import fastairag.FastAIRag;
import fastairag.RagDocument;
import fastairag.RagStore;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    private RagStore store;

    @Setup(Level.Trial)
    public void setup() {
        Random rng = new Random(42);
        EmbeddingProvider dummyEmbedder = text -> {
            float[] vec = new float[384];
            for (int i = 0; i < 384; i++) {
                vec[i] = rng.nextFloat() * 2.0f - 1.0f;
            }
            return vec;
        };

        store = FastAIRag.store(dummyEmbedder);
        for (int i = 0; i < 1000; i++) {
            store.add(new RagDocument("doc_" + i, "Technical specification passage #" + i + " outlining benchmarking parameters.", java.util.Map.of()));
        }
    }

    @org.openjdk.jmh.annotations.Benchmark
    public List<RagDocument> benchmarkContextRetrieval() {
        return store.search("benchmarking parameters", 5);
    }
}