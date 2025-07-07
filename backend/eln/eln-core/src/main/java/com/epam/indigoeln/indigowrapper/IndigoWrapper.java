package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class IndigoWrapper {

    private final Indigo indigo;
    private final IndigoRenderer renderer;

    public synchronized <T> T withSession(Function<IndigoSession, T> block) {
        IndigoSession session = new IndigoSession(indigo, renderer);
        return block.apply(session);
    }

    public synchronized void withSession(Consumer<IndigoSession> block) {
        withSession(indigoSession -> {
            block.accept(indigoSession);
            return null;
        });
    }
}
