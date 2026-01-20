package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.eln.model.ExperimentSignature;
import com.epam.indigoeln.eln.model.SignatureReason;
import com.epam.indigoeln.eln.model.SignatureStatus;
import com.epam.indigoeln.eln.model.UserRef;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.groups.Tuple;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SuppressWarnings("UnusedReturnValue")
public class SignaturesAssert extends AbstractAssert<SignaturesAssert, List<ExperimentSignature>> {

    public static SignaturesAssert assertThatSignatures(List<ExperimentSignature> actual) {
        return new SignaturesAssert(actual);
    }

    protected SignaturesAssert(List<ExperimentSignature> actual) {
        super(actual, SignaturesAssert.class);
    }

    public SignaturesAssert containsOnly(UserRef user, SignatureReason reason, @Nullable SignatureStatus status) {
        doAssert(tuple(user, reason, status));
        return this;
    }

    public SignaturesAssert containsOnly(UserRef user1, SignatureReason reason1, @Nullable SignatureStatus status1, UserRef user2, SignatureReason reason2, @Nullable SignatureStatus status2) {
        doAssert(tuple(user1, reason1, status1), tuple(user2, reason2, status2));
        return this;
    }

    private void doAssert(Tuple... tuples) {
        assertThat(actual).map(ExperimentSignature::getUser, ExperimentSignature::getReason, ExperimentSignature::getStatus).containsExactly(tuples);
    }
}
