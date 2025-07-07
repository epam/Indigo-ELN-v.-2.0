package com.epam.indigoeln.indigowrapper;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoRenderer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IndigoAPI {

    private final Indigo indigo;

    public void setOption(String option, String value) {
        indigo.setOption(option, value);
    }

    public void setOption(String option, int value) {
        indigo.setOption(option, value);
    }

    public void setOption(String option, int x, int y) {
        indigo.setOption(option, x, y);
    }

    public void setOption(String option, float r, float g, float b) {
        indigo.setOption(option, r, g, b);
    }

    public void setOption(String option, boolean value) {
        indigo.setOption(option, value);
    }

    public void setOption(String option, float value) {
        indigo.setOption(option, value);
    }

    public void setOption(String option, double value) {
        indigo.setOption(option, value);
    }

    public void resetOptions() {
        indigo.resetOptions();
    }

    public IndigoMolecule loadMolecule(byte[] buf) {
        return new IndigoMolecule(this, indigo.loadMolecule(buf));
    }

    public IndigoMolecule loadMoleculeFromBuffer(byte[] buf) {
        return new IndigoMolecule(this, indigo.loadMoleculeFromBuffer(buf));
    }

    public IndigoMolecule loadMoleculeFromFile(String path) {
        return new IndigoMolecule(this, indigo.loadMoleculeFromFile(path));
    }

    public IndigoMolecule loadMolecule(String str) {
        return new IndigoMolecule(this, indigo.loadMolecule(str));
    }

    public IndigoReaction loadReaction(byte[] buf) {
        return new IndigoReaction(this, indigo.loadReaction(buf));
    }

    public IndigoReaction loadReaction(String str) {
        return new IndigoReaction(this, indigo.loadReaction(str));
    }

    public IndigoReaction loadReactionFromFile(String path) {
        return new IndigoReaction(this, indigo.loadReactionFromFile(path));
    }
}
