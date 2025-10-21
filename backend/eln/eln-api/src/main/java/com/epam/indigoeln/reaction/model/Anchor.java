package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode(of = "str")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Anchor {

    @JsonValue
    private final String str;

    protected Anchor(String prefix, int number) {
        str = prefix + number;
    }

    @Override
    public String toString() {
        return str;
    }

    public static class Reaction extends Anchor {

        public Reaction(int number) {
            super("R", number);
        }

        @JsonCreator
        Reaction(String str) {
            super(str);
        }
    }

    public static class Input extends Anchor {

        public Input(int number) {
            super("I", number);
        }

        @JsonCreator
        Input(String str) {
            super(str);
        }
    }

    public static class InputSample extends Anchor {

        public InputSample(int number) {
            super("IS", number);
        }

        @JsonCreator
        InputSample(String str) {
            super(str);
        }
    }

    public static class Output extends Anchor {

        public Output(int number) {
            super("O", number);
        }

        @JsonCreator
        Output(String str) {
            super(str);
        }
    }

    public static class OutputSample extends Anchor {

        public OutputSample(int number) {
            super("OS", number);
        }

        @JsonCreator
        OutputSample(String str) {
            super(str);
        }
    }
}
