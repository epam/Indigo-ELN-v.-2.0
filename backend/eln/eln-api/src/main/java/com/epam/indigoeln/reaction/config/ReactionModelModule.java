package com.epam.indigoeln.reaction.config;

import com.epam.indigoeln.reaction.model.EnteredValue;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Deserializers that pass experiment-level units down to {@link EnteredValue}s.
 * Also discoverable via {@code ObjectMapper.findAndRegisterModules()}.
 */
public class ReactionModelModule extends SimpleModule {

    public ReactionModelModule() {
        setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass() == ExperimentModel.class) {
                    return new ExperimentModel.Deserializer(deserializer);
                }
                return deserializer;
            }
        });
    }
}
