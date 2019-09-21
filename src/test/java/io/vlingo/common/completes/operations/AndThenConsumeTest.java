package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.test.SinkVerifier;
import io.vlingo.common.completes.test.TestSink;
import io.vlingo.common.completes.test.TestSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class AndThenConsumeTest {
    private SinkVerifier<Integer> verifier;
    private TestSource<Integer> source;

    @Before
    public void setUp() {
        verifier = new TestSink<>();
        source = new TestSource<>();
    }

    @Test
    public void shouldProcessTheOutcomeButNotChangeIt() {
        AtomicInteger result = new AtomicInteger(0);
        verifier.outcomeIs(42);

        source.emitOutcome(42);
        source.emitCompletion();

        AndThenConsume<Integer> operation = new AndThenConsume<>(result::set);
        operation.subscribe(verifier.asSink());

        source.subscribe(operation);
        source.flush();

        Assert.assertEquals(42, result.get());
    }

    @Test
    public void shouldPassThroughErrors() {
        Throwable cause = new RuntimeException("Yay!");

        verifier.failedWith(cause);

        source.emitError(cause);
        source.emitCompletion();

        AndThenConsume<Integer> operation = new AndThenConsume<>(a -> {});
        operation.subscribe(verifier.asSink());

        source.subscribe(operation);
        source.flush();
    }

    @Test
    public void shouldEmitAnErrorIfTheMapperFails() {
        verifier.failedWith(NullPointerException.class);

        source.emitOutcome(null);
        source.emitCompletion();

        AndThenConsume<Integer> operation = new AndThenConsume<>(Object::notify);
        operation.subscribe(verifier.asSink());

        source.subscribe(operation);
        source.flush();
    }
}
