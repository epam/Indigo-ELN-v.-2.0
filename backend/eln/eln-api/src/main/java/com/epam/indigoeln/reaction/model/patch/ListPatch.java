package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.config.ListPatchSerializers;
import com.epam.indigoeln.reaction.model.patch.handler2.Patched;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Value
@JsonSerialize(using = ListPatchSerializers.Serializer.class)
@JsonDeserialize(using = ListPatchSerializers.Deserializer.class)
public class ListPatch<T, P> {

    public static final String FROM_FIELD = "$from";

    List<Item<T, P>> items;

//    public void forEach(BiConsumer<K, @Nullable T> action) {
//        for (Map.Entry<K, @Nullable T> entry : items.entrySet()) {
//            action.accept(entry.getKey(), entry.getValue());
//        }
//    }

    @SafeVarargs
    public static <T, P> ListPatch<T, P> of(Item<T, P>... items) {
        return new ListPatch<>(Arrays.asList(items));
    }

    public record Item<T, P> (
            @Nullable Integer oldIndex,
            @Nullable Integer newIndex,
            @Nullable Patched<T, P> value
    ) {
        public Item {
            Preconditions.checkArgument(oldIndex != null || newIndex != null);
        }
    }
}
