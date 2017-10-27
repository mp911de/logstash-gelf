package biz.paluch.logging.gelf.intern;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class GelfMessageBenchmark {

    @Benchmark
    public Object discoverStringField() {
        return GelfMessage.getAdditionalFieldValue("foo", GelfMessage.FIELD_TYPE_DISCOVER);
    }

    @Benchmark
    public Object discoverLongField() {
        return GelfMessage.getAdditionalFieldValue("12345", GelfMessage.FIELD_TYPE_DISCOVER);
    }

    @Benchmark
    public Object discoverDoubleField() {
        return GelfMessage.getAdditionalFieldValue("123.45", GelfMessage.FIELD_TYPE_DISCOVER);
    }

    @Benchmark
    public Object configuredStringField() {
        return GelfMessage.getAdditionalFieldValue("foo", GelfMessage.FIELD_TYPE_STRING);
    }

    @Benchmark
    public Object configuredLongField() {
        return GelfMessage.getAdditionalFieldValue("12345", GelfMessage.FIELD_TYPE_LONG);
    }

    @Benchmark
    public Object configuredDoubleField() {
        return GelfMessage.getAdditionalFieldValue("123.45", GelfMessage.FIELD_TYPE_DOUBLE);
    }

    public static void main(String[] args) throws RunnerException {

        ChainedOptionsBuilder builder = new OptionsBuilder().forks(1).warmupIterations(5).threads(1).measurementIterations(5)
                .timeout(TimeValue.seconds(2));

        new Runner(builder.mode(Mode.AverageTime).timeUnit(TimeUnit.NANOSECONDS).include(".*GelfMessageBenchmark.*").build())
                .run();
    }
}
