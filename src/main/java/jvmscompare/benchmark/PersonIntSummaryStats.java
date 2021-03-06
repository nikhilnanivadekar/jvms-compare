package jvmscompare.benchmark;

import java.util.IntSummaryStatistics;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jvmscompare.JavaInformation;
import jvmscompare.Person;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.impl.collector.Collectors2;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
public class PersonIntSummaryStats
{
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newWorkStealingPool();

    public static void main(String[] args) throws RunnerException
    {
        new JavaInformation().printJavaInformation();
        Options options = new OptionsBuilder().include(".*" + PersonIntSummaryStats.class.getSimpleName() + ".*")
                .forks(2)
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-results/person-int-summary-stats/" + args[0] + ".json")
                .warmupIterations(20)
                .measurementIterations(10)
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .build();
        new Runner(options).run();
    }


    @Benchmark
    public IntSummaryStatistics intSummaryStatisticsECEager_serial()
    {
        IntSet uniqueAges =
                Person.getECPeople().collectInt(Person::getAge, IntSets.mutable.empty());
        IntSummaryStatistics summary = uniqueAges.summaryStatistics();
        return summary;
    }

    @Benchmark
    public IntSummaryStatistics intSummaryStatisticsECLazy_serial()
    {
        IntSet uniqueAges =
                Person.getECPeople().asLazy().collectInt(Person::getAge).toSet();
        IntSummaryStatistics summary = uniqueAges.summaryStatistics();
        return summary;
    }

    @Benchmark
    public IntSummaryStatistics intSummaryStatisticsJDK_serial()
    {
        Set<Integer> uniqueAges =
                Person.getJDKPeople().stream()
                        .mapToInt(Person::getAge)
                        .boxed()
                        .collect(Collectors.toSet());
        IntSummaryStatistics summary = uniqueAges.stream().mapToInt(i -> i).summaryStatistics();
        return summary;
    }

    @Benchmark
    public IntSummaryStatistics intSummaryStatisticsECStream_parallel()
    {
        IntSet uniqueAges =
                Person.getECPeople()
                        .parallelStream()
                        .collect(Collectors2.collectInt(Person::getAge, IntSets.mutable::empty));
        IntSummaryStatistics summary = uniqueAges.summaryStatistics();
        return summary;
    }

    @Benchmark
    public IntSummaryStatistics intSummaryStatisticsJDK_parallel()
    {
        Set<Integer> uniqueAges =
                Person.getJDKPeople().parallelStream()
                        .mapToInt(Person::getAge)
                        .boxed()
                        .collect(Collectors.toSet());
        IntSummaryStatistics summary = uniqueAges.parallelStream().mapToInt(i -> i).summaryStatistics();
        return summary;
    }
}
