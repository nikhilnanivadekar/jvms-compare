package jvmscompare.benchmark;

import jvmscompare.JavaInformation;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.list.mutable.FastList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
public class IntListSum
{
    private List<Integer> jdkList;
    private IntList ecIntList;
    private MutableList<Integer> ecList;
    private ExecutorService executor;

    @Setup
    public void setUp()
    {
        this.executor = Executors.newWorkStealingPool();
        PrimitiveIterator.OfInt iterator = new Random(1L).ints(-1000, 1000).iterator();
        this.ecList = FastList.newWithNValues(1_000_000, iterator::nextInt);
        this.jdkList = new ArrayList<>(1_000_000);
        this.jdkList.addAll(this.ecList);
        this.ecIntList = this.ecList.collectInt(i -> i, new IntArrayList(1_000_000));
    }

    public static void main(String[] args) throws RunnerException
    {
        new JavaInformation().printJavaInformation();
        Options options = new OptionsBuilder().include(".*" + IntListSum.class.getSimpleName() + ".*")
                .forks(2)
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-results/int-list-sum/" + args[0] + ".json")
                .warmupIterations(20)
                .measurementIterations(10)
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .build();
        new Runner(options).run();
    }

    @Benchmark
    public long sumJDK()
    {
        return this.jdkList.stream().mapToLong(i -> i).sum();
    }

    @Benchmark
    public long sumJDKParallel()
    {
        return this.jdkList.parallelStream().mapToLong(i -> i).sum();
    }

    @Benchmark
    public long sumECPrimitive()
    {
        return this.ecIntList.sum();
    }

    @Benchmark
    public long sumEC()
    {
        return this.ecList.sumOfInt(i -> i);
    }

    @Benchmark
    public long sumECParallel()
    {
        return this.ecList.asParallel(this.executor, 100_000).sumOfInt(i -> i);
    }

}
