package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.patch.AbstractListElementPatch;
import com.epam.indigoeln.reaction.model.patch.ListPatch;
import com.epam.indigoeln.reaction.util.Flag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

// (no entry): doesn't change
// index -> patch: value updated
// index -> {$from: oldIndex}: item repositioned
// index -> {$from: oldIndex, ...}: item repositioned and updated
// index -> null: item deleted
@SuppressWarnings("OptionalAssignedToNull")
@RequiredArgsConstructor
public class ListValueHandler<C, T, A extends Anchor, P extends AbstractListElementPatch<A>> extends AbstractValueHandler<C, List<T>, ListPatch<Integer, P>> {

    private final Function<T, A> anchorFn;
    private final AbstractValueHandler<C, T, P> itemHandler;

    @Override
    protected ListPatch<Integer, P> doCompare(Flag updated, @Nullable List<T> a, List<T> b, @Nullable Optional<Integer> from) {
        Map<A, Comparison<T>> map = new HashMap<>();
        for (int i = 0; i < b.size(); i++) {
            T item = b.get(i);
            A anchor = anchorFn.apply(item);
            Comparison<T> c = map.computeIfAbsent(anchor, x -> new Comparison<>());
            c.newIndex = i;
            c.newItem = item;
        }
        if (a != null) {
            for (int i = 0; i < a.size(); i++) {
                T item = a.get(i);
                A anchor = anchorFn.apply(item);
                Comparison<T> c = map.computeIfAbsent(anchor, x -> new Comparison<>());
                c.oldIndex = i;
                c.oldItem = item;
            }
        }
        Flag collectionUpdated = new Flag();
        Set<Integer> removed = new HashSet<>();
        ListPatch<Integer, P> result = new ListPatch<>(b.size());
        map.forEach((anchor, c) -> {
            Optional<Integer> itemFrom;
            if (c.oldItem == null) {
                itemFrom = Optional.empty();
            } else if (c.newIndex != c.oldIndex) {
                itemFrom = Optional.of(c.oldIndex);
            } else {
                itemFrom = null;
            }
            Optional<P> patch = itemHandler.compare(collectionUpdated, c.oldItem, c.newItem, itemFrom);
            if (patch != null) {
                if (patch.isPresent()) {
                    result.getItems().put(c.newIndex, patch.get());
                } else {
                    removed.add(c.oldIndex);
                }
                updated.set();
            }
        });
        for (Integer i : removed) {
            if (i < result.getSize()) {
                result.getItems().putIfAbsent(i, null); // mark as deleted, but only if no other item moved to this position
            }
        }
        return result;
    }

    private static class Comparison<T> {

        int oldIndex = -1;
        int newIndex = -1;
        @Nullable
        T oldItem = null;
        @Nullable
        T newItem = null;
    }
}
