/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022-2030 The XdagJ Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.xdag.crypto.randomx;

import java.util.List;
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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@Threads(value = 1)
@BenchmarkMode({ Mode.Throughput, Mode.AverageTime})
@Measurement(iterations = 5, time = 10)
@Warmup(iterations = 1, time = 10)
@OutputTimeUnit(TimeUnit.SECONDS)
public class RandomXJNAPerfTest {

    private RandomXVM randomxVm;

    private final Random random = new Random();

    @Setup(Level.Trial)
    public void setup() {
        RandomXWrapper randomXWrapper = RandomXWrapper.builder()
                .flags(List.of(RandomXFlag.JIT, RandomXFlag.HARD_AES, RandomXFlag.ARGON2))
                .fastInit(true)
                .miningMode(false)
                .build();
        byte[] buffer = new byte[32];
        random.nextBytes(buffer);
        randomXWrapper.init(buffer);
        randomxVm = randomXWrapper.createVM();
    }

    @Benchmark
    public byte[] testHash() {
        byte[] buffer = new byte[32];
        random.nextBytes(buffer);
        return randomxVm.getHash(buffer);
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
