package com.avella.shared.unit;

import com.avella.shared.application.Command;
import com.avella.shared.application.CommandHandler;
import com.avella.shared.application.decorator.RetryNonBusinessErrorDecorator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryNonBusinessErrorDecoratorTest {

    record TestCommand() implements Command {
    }

    class SomeCustomException extends RuntimeException {
        public SomeCustomException(String message) {
            super(message);
        }
    }

    class FailingHandler implements CommandHandler<TestCommand> {
        private int nbFailLeft;

        public FailingHandler(int nbFailLeft) {
            this.nbFailLeft = nbFailLeft;
        }

        @Override
        public void handle(TestCommand command) {
            if (nbFailLeft > 0) {
                nbFailLeft--;
                throw new SomeCustomException("Expected failure");
            }
        }
    }

    @Test
    void correctlyRetry() {
        var handler = new RetryNonBusinessErrorDecorator<>(new FailingHandler(3), 3);
        assertDoesNotThrow(() -> handler.handle(new TestCommand()));
    }

    @Test
    void throwWhenOutOfRetryLeft() {
        var handler = new RetryNonBusinessErrorDecorator<>(new FailingHandler(3), 2);
        var error = assertThrows(SomeCustomException.class, () -> handler.handle(new TestCommand()));
        assertEquals("Expected failure", error.getMessage());
    }
}
