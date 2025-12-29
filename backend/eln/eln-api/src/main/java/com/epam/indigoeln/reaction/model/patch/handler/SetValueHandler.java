package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.util.Flag;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

// (no entry): doesn't change
// id -> {...}: value added or updated
// id -> null: value deleted
@SuppressWarnings("OptionalAssignedToNull")
@RequiredArgsConstructor
public class SetValueHandler<C, T, A extends Comparable<A>, P> extends AbstractValueHandler<C, Set<T>, ListPatch<A, P>> {

    private final Function<T, A> anchorFn;
    private final AbstractValueHandler<C, T, P> itemHandler;

    @Override
    protected ListPatch<A, P> doCompare(Flag updated, @Nullable Set<T> a, Set<T> b, @Nullable Optional<Integer> from) {
        Map<A, Comparison<T>> map = new HashMap<>();
        for (T item : b) {
            A anchor = anchorFn.apply(item);
            Comparison<T> c = map.computeIfAbsent(anchor, x -> new Comparison<>());
            c.newItem = item;
        }
        if (a != null) {
            for (T item : a) {
                A anchor = anchorFn.apply(item);
                Comparison<T> c = map.computeIfAbsent(anchor, x -> new Comparison<>());
                c.oldItem = item;
            }
        }
        Flag collectionUpdated = new Flag();
        ListPatch<A, P> result = new ListPatch<>(b.size());
        map.forEach((anchor, c) -> {
            Optional<P> patch = itemHandler.compare(collectionUpdated, c.oldItem, c.newItem, null);
            if (patch != null) {
                result.getItems().put(anchor, patch.orElse(null));
                updated.set();
            }
        });
        return result;
    }

    @Override
    protected Set<T> doApply(C container, @Nullable Set<T> value, ListPatch<A, P> patch) {
        Map<A, T> map = StreamEx.of(value != null ? value : Set.of())
                .mapToEntry(anchorFn, Function.identity())
                .toCustomMap(LinkedHashMap::new);
        patch.forEach((anchor, itemPatch) -> {
            if (itemPatch == null) {
                map.remove(anchor);
            } else {
                T newValue = itemHandler.doApply(container, map.get(anchor), itemPatch);
                map.put(anchor, newValue);
            }
        });
        return new LinkedHashSet<>(map.values());
    }

    private static class Comparison<T> {

        @Nullable
        T oldItem = null;
        @Nullable
        T newItem = null;
    }
}
