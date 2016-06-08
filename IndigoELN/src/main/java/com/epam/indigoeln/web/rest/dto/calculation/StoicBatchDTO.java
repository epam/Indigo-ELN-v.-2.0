package com.epam.indigoeln.web.rest.dto.calculation;


import com.epam.indigoeln.web.rest.dto.common.StringValueDTO;
import com.epam.indigoeln.web.rest.dto.common.UnitValueDTO;

public class StoicBatchDTO {

    private boolean limiting;
    private UnitValueDTO weight;
    private UnitValueDTO volume;
    private UnitValueDTO density;
    private UnitValueDTO molarity;
    private UnitValueDTO mol;
    private UnitValueDTO loadFactor;
    private UnitValueDTO theoWeight;
    private UnitValueDTO theoMoles;
    private StringValueDTO rxnRole;
    private StringValueDTO saltCode;
    private double molWeight;
    private double stoicPurity;
    private double saltEq;
    private double eq;
    private double yield;

    public boolean isLimiting() {
        return limiting;
    }

    public void setLimiting(boolean limiting) {
        this.limiting = limiting;
    }

    public UnitValueDTO getWeight() {
        return weight;
    }

    public void setWeight(UnitValueDTO weight) {
        this.weight = weight;
    }

    public UnitValueDTO getVolume() {
        return volume;
    }

    public void setVolume(UnitValueDTO volume) {
        this.volume = volume;
    }

    public UnitValueDTO getDensity() {
        return density;
    }

    public void setDensity(UnitValueDTO density) {
        this.density = density;
    }

    public UnitValueDTO getMolarity() {
        return molarity;
    }

    public void setMolarity(UnitValueDTO molarity) {
        this.molarity = molarity;
    }

    public UnitValueDTO getMol() {
        return mol;
    }

    public void setMol(UnitValueDTO mol) {
        this.mol = mol;
    }

    public UnitValueDTO getLoadFactor() {
        return loadFactor;
    }

    public void setLoadFactor(UnitValueDTO loadFactor) {
        this.loadFactor = loadFactor;
    }

    public UnitValueDTO getTheoWeight() {
        return theoWeight;
    }

    public void setTheoWeight(UnitValueDTO theoWeight) {
        this.theoWeight = theoWeight;
    }

    public UnitValueDTO getTheoMoles() {
        return theoMoles;
    }

    public void setTheoMoles(UnitValueDTO theoMoles) {
        this.theoMoles = theoMoles;
    }

    public StringValueDTO getRxnRole() {
        return rxnRole;
    }

    public void setRxnRole(StringValueDTO rxnRole) {
        this.rxnRole = rxnRole;
    }

    public StringValueDTO getSaltCode() {
        return saltCode;
    }

    public void setSaltCode(StringValueDTO saltCode) {
        this.saltCode = saltCode;
    }

    public double getMolWeight() {
        return molWeight;
    }

    public void setMolWeight(double molWeight) {
        this.molWeight = molWeight;
    }

    public double getStoicPurity() {
        return stoicPurity;
    }

    public void setStoicPurity(double stoicPurity) {
        this.stoicPurity = stoicPurity;
    }

    public double getSaltEq() {
        return saltEq;
    }

    public void setSaltEq(double saltEq) {
        this.saltEq = saltEq;
    }

    public double getEq() {
        return eq;
    }

    public void setEq(double eq) {
        this.eq = eq;
    }

    public double getYield() {
        return yield;
    }

    public void setYield(double yield) {
        this.yield = yield;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoicBatchDTO myBatch = (StoicBatchDTO) o;

        if (limiting != myBatch.limiting) return false;
        if (Double.compare(myBatch.molWeight, molWeight) != 0) return false;
        if (Double.compare(myBatch.stoicPurity, stoicPurity) != 0) return false;
        if (Double.compare(myBatch.saltEq, saltEq) != 0) return false;
        if (Double.compare(myBatch.eq, eq) != 0) return false;
        if (weight != null ? !weight.equals(myBatch.weight) : myBatch.weight != null) return false;
        if (volume != null ? !volume.equals(myBatch.volume) : myBatch.volume != null) return false;
        if (density != null ? !density.equals(myBatch.density) : myBatch.density != null) return false;
        if (molarity != null ? !molarity.equals(myBatch.molarity) : myBatch.molarity != null) return false;
        if (mol != null ? !mol.equals(myBatch.mol) : myBatch.mol != null) return false;
        if (loadFactor != null ? !loadFactor.equals(myBatch.loadFactor) : myBatch.loadFactor != null) return false;
        if (rxnRole != null ? !rxnRole.equals(myBatch.rxnRole) : myBatch.rxnRole != null) return false;
        return saltCode != null ? saltCode.equals(myBatch.saltCode) : myBatch.saltCode == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (limiting ? 1 : 0);
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        result = 31 * result + (volume != null ? volume.hashCode() : 0);
        result = 31 * result + (density != null ? density.hashCode() : 0);
        result = 31 * result + (molarity != null ? molarity.hashCode() : 0);
        result = 31 * result + (mol != null ? mol.hashCode() : 0);
        result = 31 * result + (loadFactor != null ? loadFactor.hashCode() : 0);
        result = 31 * result + (rxnRole != null ? rxnRole.hashCode() : 0);
        result = 31 * result + (saltCode != null ? saltCode.hashCode() : 0);
        temp = Double.doubleToLongBits(molWeight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(stoicPurity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(saltEq);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(eq);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
