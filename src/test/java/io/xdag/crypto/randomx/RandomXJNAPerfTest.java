package io.xdag.crypto.randomx;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.common.collect.Lists;

@State(Scope.Benchmark)
@Threads(value = 1)
@BenchmarkMode({ Mode.Throughput, Mode.AverageTime})
@Measurement(iterations = 5, time = 10)
@Warmup(iterations = 1, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
public class RandomXJNAPerfTest {

    private RandomXVM randomxVm;

    @Setup(Level.Trial)
    public void setup() {
        RandomXWrapper randomXWrapper = RandomXWrapper.builder()
                .flags(Lists.newArrayList(RandomXWrapper.Flag.JIT))
                .fastInit(true)
                .build();
        randomXWrapper.init(RandomUtils.nextBytes(32));
        randomxVm = randomXWrapper.createVM();
    }

    @Benchmark
    public byte[] testHash() {
        return randomxVm.getHash(RandomUtils.nextBytes(32));
    }

    @Test
    public void main() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RandomXJNAPerfTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
