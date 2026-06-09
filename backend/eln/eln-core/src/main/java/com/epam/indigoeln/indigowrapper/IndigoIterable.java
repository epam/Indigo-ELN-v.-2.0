package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.IndigoObject;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class IndigoIterable<O extends AbstractIndigoObject> implements Iterable<O> {

    private final Supplier<IndigoObject> iteratorCreator;
    private final Function<IndigoObject, O> objectCreator;

    @Override
    public Iterator<O> iterator() {
        IndigoObject iter = iteratorCreator.get();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public O next() {
                IndigoObject next = iter.next();
                return objectCreator.apply(next);
            }

            @Override
            public void remove() {
                iter.remove();
            }
        };
    }
}
