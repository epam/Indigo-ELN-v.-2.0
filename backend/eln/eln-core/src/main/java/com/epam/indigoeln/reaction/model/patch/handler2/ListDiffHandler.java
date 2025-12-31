package com.epam.indigoeln.reaction.model.patch.handler2;

import com.epam.indigoeln.reaction.model.patch.ListPatch;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

// (no entry): doesn't change
// ">index" -> {...}: value inserted
// "index" -> {...}: value updated
// "oldIndex->index" -> "$unchanged": item repositioned
// "oldIndex->index -> {...}: item repositioned and updated
// "index" -> {"$old": {...}}: item deleted
@RequiredArgsConstructor
public class ListDiffHandler<I, K, P> extends AbstractDiffHandler<List<I>, ListPatch<P>> {

    private static final Comparator<ListPatch.Item<?>> ITEM_COMPARATOR = Comparator
            // first, deleted items (-1); second, order by newIndex
            .comparing((ListPatch.Item<?> i) -> i.newIndex() != null ? i.newIndex() : -1)
            // for deleted items, sort by old index
            .thenComparing(i -> i.oldIndex() != null ? i.oldIndex() : -1);

    private final Function<I, K> keyFn;
    private final DiffHandler<I, P> itemHandler;

    @Override
    protected Patched<ListPatch<P>> doCompare(@Nullable List<I> a, List<I> b) {
        Map<K, Comparison<I>> map = new HashMap<>();
        for (int i = 0; i < b.size(); i++) {
            I item = b.get(i);
            K anchor = keyFn.apply(item);
            Comparison<I> c = map.computeIfAbsent(anchor, x -> new Comparison<>());
            c.newIndex = i;
            c.newItem = item;
        }
        if (a != null) {
            for (int i = 0; i < a.size(); i++) {
                I item = a.get(i);
                K anchor = keyFn.apply(item);
                Comparison<I> c = map.computeIfAbsent(anchor, x -> new Comparison<>());
                c.oldIndex = i;
                c.oldItem = item;
            }
        }
        List<ListPatch.Item<P>> result = new ArrayList<>(map.size() * 2);
        map.forEach((anchor, c) -> {
            Patched<P> patch = itemHandler.compare(c.oldItem, c.newItem);
            if (c.newIndex != c.oldIndex || patch != null) {
                result.add(new ListPatch.Item<>(c.oldIndex != -1 ? c.oldIndex : null, c.newIndex != -1 ? c.newIndex : null, patch));
            }
        });
        if (result.isEmpty()) {
            return null;
        }
        result.sort(ITEM_COMPARATOR);
        return Patched.verbatim(new ListPatch<>(result));
    }

    private static class Comparison<I> {

        int oldIndex = -1;
        int newIndex = -1;
        @Nullable
        I oldItem;
        @Nullable
        I newItem;
    }
}
