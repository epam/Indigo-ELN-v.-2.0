package com.epam.indigoeln.reaction.model.patch.handler2;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor
public class SetDiffHandler<I, C extends Collection<I>, K, P> extends AbstractDiffHandler<C, Map<K, Patched<P>>> {

    private final Function<I, K> keyFn;
    private final DiffHandler<I, P> itemHandler;

    @Override
    protected boolean isEmpty(@Nullable C value) {
        return value == null || value.isEmpty();
    }

    @Override
    protected Patched<Map<K, Patched<P>>> doDeleted(C a) {
        //noinspection unchecked
        return doCompare(a, (C) List.of());
    }

    @Override
    protected Patched<Map<K, Patched<P>>> doCreated(C b) {
        //noinspection unchecked
        return doCompare((C) List.of(), b);
    }

    @Override
    @Nullable
    protected Patched<Map<K, Patched<P>>> doCompare(@Nullable C a, C b) {
        Map<K, Comparison<I>> map = new HashMap<>();
        for (I item : b) {
            K anchor = keyFn.apply(item);
            Comparison<I> c = map.computeIfAbsent(anchor, x -> new Comparison<>());
            c.newItem = item;
        }
        if (a != null) {
            for (I item : a) {
                K anchor = keyFn.apply(item);
                Comparison<I> c = map.computeIfAbsent(anchor, x -> new Comparison<>());
                c.oldItem = item;
            }
        }
        Map<K, Patched<P>> result = new LinkedHashMap<>();
        map.forEach((anchor, c) -> {
            Patched<P> patch = itemHandler.compare(c.oldItem, c.newItem);
            if (patch != null) {
                result.put(anchor, patch);
            }
        });
        return !result.isEmpty() ? Patched.verbatim(result) : null;
    }

    private static class Comparison<T> {

        @Nullable
        T oldItem = null;
        @Nullable
        T newItem = null;
    }
}
