package jvmscompare.benchmark;

import jvmscompare.JavaInformation;
import jvmscompare.Person;
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

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(2)
public class PersonCombinedSummaryStats
{
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newWorkStealingPool();

    public static void main(String[] args) throws RunnerException
    {
        new JavaInformation().printJavaInformation();
        Options options = new OptionsBuilder().include(".*" + PersonCombinedSummaryStats.class.getSimpleName() + ".*")
                .forks(2)
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-results/person-combined-summary-stats/" + args[0] + ".json")
                .warmupIterations(20)
                .measurementIterations(10)
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.SECONDS)
                .build();
        new Runner(options).run();
    }

    @Benchmark
    public Object[] combinedStatisticsJDK_parallel()
    {
        DoubleSummaryStatistics stats1 =
                Person.getJDKPeople().parallelStream().mapToDouble(Person::getHeightInInches).summaryStatistics();
        DoubleSummaryStatistics stats2 =
                Person.getJDKPeople().parallelStream().mapToDouble(Person::getWeightInPounds).summaryStatistics();
        IntSummaryStatistics stats3 =
                Person.getJDKPeople().parallelStream().mapToInt(Person::getAge).summaryStatistics();
        return new Object[]{stats1, stats2, stats3};
    }

    @Benchmark
    public Object[] combinedStatisticsECStream_parallel()
    {
        DoubleSummaryStatistics stats1 =
                Person.getECPeople().parallelStream().mapToDouble(Person::getHeightInInches).summaryStatistics();
        DoubleSummaryStatistics stats2 =
                Person.getECPeople().parallelStream().mapToDouble(Person::getWeightInPounds).summaryStatistics();
        IntSummaryStatistics stats3 =
                Person.getECPeople().parallelStream().mapToInt(Person::getAge).summaryStatistics();
        return new Object[]{stats1, stats2, stats3};
    }

    @Benchmark
    public Object[] combinedStatisticsJDK_serial()
    {
        DoubleSummaryStatistics stats1 =
                Person.getJDKPeople().stream().mapToDouble(Person::getHeightInInches).summaryStatistics();
        DoubleSummaryStatistics stats2 =
                Person.getJDKPeople().stream().mapToDouble(Person::getWeightInPounds).summaryStatistics();
        IntSummaryStatistics stats3 =
                Person.getJDKPeople().stream().mapToInt(Person::getAge).summaryStatistics();
        return new Object[]{stats1, stats2, stats3};
    }

    @Benchmark
    public Object[] combinedStatisticsECLazy_serial()
    {
        DoubleSummaryStatistics stats1 =
                Person.getECPeople().asLazy().collectDouble(Person::getHeightInInches).summaryStatistics();
        DoubleSummaryStatistics stats2 =
                Person.getECPeople().asLazy().collectDouble(Person::getWeightInPounds).summaryStatistics();
        IntSummaryStatistics stats3 =
                Person.getECPeople().asLazy().collectInt(Person::getAge).summaryStatistics();
        return new Object[]{stats1, stats2, stats3};
    }

    @Benchmark
    public Object[] combinedStatisticsECEager_serial()
    {
        DoubleSummaryStatistics stats1 =
                Person.getECPeople().summarizeDouble(Person::getHeightInInches);
        DoubleSummaryStatistics stats2 =
                Person.getECPeople().summarizeDouble(Person::getWeightInPounds);
        IntSummaryStatistics stats3 =
                Person.getECPeople().summarizeInt(Person::getAge);
        return new Object[]{stats1, stats2, stats3};
    }

}
