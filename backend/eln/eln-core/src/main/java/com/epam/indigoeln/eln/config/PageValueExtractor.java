package com.epam.indigoeln.eln.config;

import com.epam.indigoeln.eln.model.Page;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;

@UnwrapByDefault
public class PageValueExtractor implements ValueExtractor<Page<@ExtractedValue ?>> {

    @Override
    public void extractValues(Page<?> originalValue, ValueReceiver receiver) {
        for (int i = 0; i < originalValue.getItems().size(); i++) {
            receiver.indexedValue("<page element>", i, originalValue.getItems().get(i));
        }
    }
}
