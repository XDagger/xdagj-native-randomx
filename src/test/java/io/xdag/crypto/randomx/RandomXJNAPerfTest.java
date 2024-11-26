package io.xdag.crypto.randomx;

import java.util.EnumSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@Threads(value = 1)
@BenchmarkMode({ Mode.Throughput, Mode.AverageTime})
@Measurement(iterations = 1, time = 100)
@OutputTimeUnit(TimeUnit.SECONDS)
public class RandomXJNAPerfTest {

    private RandomXTemplate template;
    private final Random random = new Random();

    @Setup(Level.Trial)
    public void setup() {
        byte[] buffer = new byte[32];
        random.nextBytes(buffer);
        template = RandomXTemplate.builder()
                .miningMode(false)
                .flags(EnumSet.of(RandomXFlag.DEFAULT, RandomXFlag.HARD_AES, RandomXFlag.SECURE))
                .build();
        template.init(buffer);
    }

    @Benchmark
    public byte[] testCalculateHash() {
        byte[] buffer = new byte[32];
        random.nextBytes(buffer);
        return template.calculateHash(buffer);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RandomXJNAPerfTest.class.getSimpleName())
                .result("result.json")
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(opt).run();
    }
}