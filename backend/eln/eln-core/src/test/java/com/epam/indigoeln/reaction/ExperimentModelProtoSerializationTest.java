package com.epam.indigoeln.reaction;

import com.epam.indigoeln.eln.ELNBaseTest;
import com.epam.indigoeln.reaction.mapper.ExperimentModelProtoMapper;
import com.epam.indigoeln.reaction.model.ExperimentModel;
import com.epam.indigoeln.reaction.proto.ExperimentModelProto;
import com.google.protobuf.CodedOutputStream;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class ExperimentModelProtoSerializationTest extends ELNBaseTest {

    @Inject
    ExperimentModelProtoMapper mapper;

    @Test
    void testProtoSerialization() throws IOException {
        ExperimentModel model = new ExperimentModel();
        model.setRevision(100);
        ExperimentModelProto proto = mapper.toProto(model);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CodedOutputStream cos = CodedOutputStream.newInstance(baos);
        proto.writeTo(cos);
        cos.flush();

        proto = ExperimentModelProto.parseFrom(baos.toByteArray());
        model = mapper.toModel(proto);

        assertThat(model.getRevision()).isEqualTo(model.getRevision());
    }
}
