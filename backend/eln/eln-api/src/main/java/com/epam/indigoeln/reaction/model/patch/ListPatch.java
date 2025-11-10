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
public class ListPatch<T> {

    int size;
    Map<Integer, @Nullable T> items;

    public ListPatch(int size) {
        this.size = size;
        items = new TreeMap<>();
    }

    public void forEach(BiConsumer<Integer, @Nullable T> action) {
        for (Map.Entry<Integer, @Nullable T> entry : items.entrySet()) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }

}
