package biz.paluch.logging.gelf;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import biz.paluch.logging.gelf.intern.GelfMessage;

/**
 * @author Mark Paluch
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class GelfMessageAssemblerBenchmark {

    @State(Scope.Thread)
    public static class Input {

        public Blackhole bh;
        public ByteBuffer heap = ByteBuffer.allocate(1000 * 1000);
        public ByteBuffer heap1 = ByteBuffer.allocate(1000 * 1000);
        public ByteBuffer direct = ByteBuffer.allocateDirect(1000 * 1000);
        public ByteBuffer direct1 = ByteBuffer.allocateDirect(1000 * 1000);
        public GelfMessageAssembler assembler = new GelfMessageAssembler();

        public LogEvent logEvent = new LogEvent() {
            @Override
            public String getMessage() {
                return "hello";
            }

            @Override
            public Object[] getParameters() {
                return new Object[0];
            }

            @Override
            public Throwable getThrowable() {
                return null;
            }

            @Override
            public long getLogTimestamp() {
                return 0;
            }

            @Override
            public String getSyslogLevel() {
                return "5";
            }

            @Override
            public Values getValues(MessageField field) {
                return null;
            }

            @Override
            public String getMdcValue(String mdcName) {
                return null;
            }

            @Override
            public Set<String> getMdcNames() {
                return Collections.emptySet();
            }
        };

        @Setup
        public void setup(final Blackhole bh) {
            this.bh = bh;
            this.assembler.setHost("host");
            this.assembler.setOriginHost("host");
        }
    }

    @Benchmark
    public void createMessage(Input input) {
        input.bh.consume(input.assembler.createGelfMessage(input.logEvent));
    }

    @Benchmark
    public void createMessageToJSON(Input input) {

        GelfMessage gelfMessage = input.assembler.createGelfMessage(input.logEvent);
        input.bh.consume(gelfMessage.toJson());
    }

    @Benchmark
    public void createMessageToJSONPooledHeap(Input input) {

        GelfMessage gelfMessage = input.assembler.createGelfMessage(input.logEvent);
        input.heap.clear();
        gelfMessage.toJson(input.heap, "_");
    }

    @Benchmark
    public void createMessageToJSONPooledDirect(Input input) {

        GelfMessage gelfMessage = input.assembler.createGelfMessage(input.logEvent);
        input.direct.clear();
        gelfMessage.toJson(input.direct, "_");
    }

    @Benchmark
    public void createMessageToTCPBuffer(Input input) {

        GelfMessage gelfMessage = input.assembler.createGelfMessage(input.logEvent);
        input.bh.consume(gelfMessage.toTCPBuffer());
    }

    @Benchmark
    public void createMessageToTCPBufferPooledHeap(Input input) {
        GelfMessage gelfMessage = input.assembler.createGelfMessage(input.logEvent);
        input.heap.clear();
        gelfMessage.toTCPBuffer(input.heap);
    }

    @Benchmark
    public void createMessageToTCPPooledDirect(Input input) {
        GelfMessage gelfMessage = input.assembler.createGelfMessage(input.logEvent);
        input.direct.clear();
        gelfMessage.toTCPBuffer(input.direct);
    }

    @Benchmark
    public void createMessageToUDPBuffer(Input input) {

        GelfMessage gelfMessage = input.assembler.createGelfMessage(input.logEvent);
        input.bh.consume(gelfMessage.toUDPBuffers());
    }

    @Benchmark
    public void createMessageToUDPBufferPooledHeap(Input input) {

        GelfMessage gelfMessage = input.assembler.createGelfMessage(input.logEvent);
        input.heap.clear();
        input.heap1.clear();

        input.bh.consume(gelfMessage.toUDPBuffers(input.heap, input.heap1));
    }

    @Benchmark
    public void createMessageToUDPBufferPooledDirect(Input input) {

        GelfMessage gelfMessage = input.assembler.createGelfMessage(input.logEvent);

        input.direct.clear();
        input.direct1.clear();

        input.bh.consume(gelfMessage.toUDPBuffers(input.direct, input.direct1));
    }

    public static void main(String[] args) {

        GelfMessageAssemblerBenchmark perf = new GelfMessageAssemblerBenchmark();
        Input input = new Input();
        input.setup(null);

        perf.createMessage(input);
    }
}
