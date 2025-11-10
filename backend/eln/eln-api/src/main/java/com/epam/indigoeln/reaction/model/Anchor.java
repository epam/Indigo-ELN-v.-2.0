package com.epam.indigoeln.reaction.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(of = "str")
public abstract class Anchor {

    @JsonValue
    private final String str;

    @Getter
    @JsonIgnore
    private final int number;

    protected Anchor(String prefix, int number) {
        str = prefix + number;
        this.number = number;
    }

    protected Anchor(String prefix, String str) {
        this.str = str;
        this.number = Integer.parseInt(str.substring(prefix.length()));
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
            super("R", str);
        }
    }

    public static class Input extends Anchor {

        public Input(int number) {
            super("I", number);
        }

        @JsonCreator
        Input(String str) {
            super("I", str);
        }
    }

    public static class InputSample extends Anchor {

        public InputSample(int number) {
            super("IS", number);
        }

        @JsonCreator
        InputSample(String str) {
            super("IS", str);
        }
    }

    public static class Output extends Anchor {

        public Output(int number) {
            super("O", number);
        }

        @JsonCreator
        Output(String str) {
            super("O", str);
        }
    }

    public static class OutputSample extends Anchor {

        public OutputSample(int number) {
            super("OS", number);
        }

        @JsonCreator
        OutputSample(String str) {
            super("OS", str);
        }
    }
}
