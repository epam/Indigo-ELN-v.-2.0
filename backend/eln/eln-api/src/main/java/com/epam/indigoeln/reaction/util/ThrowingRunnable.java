package com.epam.indigoeln.reaction.util;

import java.util.concurrent.Callable;

public interface ThrowingRunnable {

    void run() throws Exception;

    default Callable<?> asCallable() {
        return () -> {
            run();
            return null;
        };
    }
}
