package com.epam.indigoeln.reaction.model.patch;

import com.epam.indigoeln.reaction.config.ListPatchDeserializer;
import com.epam.indigoeln.reaction.config.ListPatchSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

@Value
@AllArgsConstructor
@JsonSerialize(using = ListPatchSerializer.class)
@JsonDeserialize(using = ListPatchDeserializer.class)
public class ListPatch<K extends Comparable<K>, T> {

    public static final String SIZE_FIELD = "$size";

    int size;
    Map<K, @Nullable T> items;

    public ListPatch(int size) {
        this.size = size;
        items = new TreeMap<>();
    }

    public void forEach(BiConsumer<K, @Nullable T> action) {
        for (Map.Entry<K, @Nullable T> entry : items.entrySet()) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }

}
