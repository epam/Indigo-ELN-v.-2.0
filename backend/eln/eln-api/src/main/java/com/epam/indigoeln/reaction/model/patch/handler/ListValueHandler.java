package com.epam.indigoeln.reaction.model.patch.handler;

import com.epam.indigoeln.reaction.model.Anchor;
import com.epam.indigoeln.reaction.model.patch.AbstractListElementPatch;
import com.epam.indigoeln.reaction.util.Flag;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

// (no entry): doesn't change
// index -> patch: value updated
// index -> {repositioned: oldIndex}: item repositioned
// index -> {repositioned: oldIndex, ...}: item repositioned and updated
// index -> null: item deleted
@SuppressWarnings("OptionalAssignedToNull")
@RequiredArgsConstructor
public class ListValueHandler<C, T, A extends Anchor, P extends AbstractListElementPatch<A>> extends AbstractValueHandler<C, List<T>, Map<Integer, @Nullable P>> {

    private final Function<T, A> anchorFn;
    private final AbstractValueHandler<C, T, P> itemHandler;

    @Override
    protected Map<Integer, @Nullable P> doCompare(Flag updated, @Nullable List<T> a, List<T> b, @Nullable Optional<Integer> from) {
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
        Map<Integer, @Nullable P> result = new TreeMap<>();
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
                    result.put(c.newIndex, patch.get());
                } else {
                    removed.add(c.oldIndex);
                }
                updated.set();
            }
        });
        for (Integer i : removed) {
            result.putIfAbsent(i, null);
        }
        return result;
    }

    @Override
    protected List<T> doApply(C container, @Nullable List<T> value, Map<Integer, @Nullable P> patch) {
        // noinspection unchecked
        T[] source = (T[]) (value != null ? value.toArray() : new Object[0]);
        T[] target = Arrays.copyOf(source, source.length + patch.size());
        for (Map.Entry<Integer, @Nullable P> entry : patch.entrySet()) {
            int position = entry.getKey();
            P itemPatch = entry.getValue();
            if (itemPatch == null) {
                target[position] = null;
            } else {
                T item;
                if (itemPatch.getXfrom() == null) {
                    item = source[position];
                } else if (itemPatch.getXfrom().isEmpty()) {
                    item = null;
                } else {
                    item = source[itemPatch.getXfrom().get()];
                }
                item = itemHandler.doApply(container, item, itemPatch);
                target[position] = item;
            }
        }
        int lastUsedPosition = target.length - 1;
        while (lastUsedPosition >= 0 && target[lastUsedPosition] == null) {
            lastUsedPosition--;
        }
        return Lists.newArrayList(Arrays.copyOf(target, lastUsedPosition + 1));
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
