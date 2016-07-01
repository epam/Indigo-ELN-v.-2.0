package com.epam.indigoeln.web.rest.dto.calculation;


import com.epam.indigoeln.web.rest.dto.calculation.common.ScalarValueDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.StringValueDTO;
import com.epam.indigoeln.web.rest.dto.calculation.common.UnitValueDTO;

public class BasicBatchModel {

    private boolean limiting;
    private UnitValueDTO weight;
    private UnitValueDTO volume;
    private UnitValueDTO density;
    private UnitValueDTO molarity; // molarAmount
    private UnitValueDTO mol; // moleAmount
    private UnitValueDTO loadFactor;
    private UnitValueDTO theoWeight; // theoreticalWeightAmount; - product
    private UnitValueDTO theoMoles; // theoreticalMoleAmount; - product
    private UnitValueDTO totalVolume; // totalVolume
    private UnitValueDTO totalWeight; // totalWeight
    private UnitValueDTO totalMoles; //totalMolarity
    private StringValueDTO rxnRole; // batchType
    private StringValueDTO saltCode;
    private ScalarValueDTO molWeight; // molecularWeightAmount
    private ScalarValueDTO stoicPurity;
    private ScalarValueDTO saltEq;
    private ScalarValueDTO eq; // rxnEq
    private double yield; // theoreticalYieldPercentAmount; - product
    private String lastUpdatedType; // weight, volume, mol, totalWeight, totalVolume
    private UnitValueDTO prevMolarAmount; // needed for solvent

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

    public ScalarValueDTO getMolWeight() {
        return molWeight;
    }

    public void setMolWeight(ScalarValueDTO molWeight) {
        this.molWeight = molWeight;
    }

    public ScalarValueDTO getStoicPurity() {
        return stoicPurity;
    }

    public void setStoicPurity(ScalarValueDTO stoicPurity) {
        this.stoicPurity = stoicPurity;
    }

    public ScalarValueDTO getSaltEq() {
        return saltEq;
    }

    public void setSaltEq(ScalarValueDTO saltEq) {
        this.saltEq = saltEq;
    }

    public ScalarValueDTO getEq() {
        return eq;
    }

    public void setEq(ScalarValueDTO eq) {
        this.eq = eq;
    }

    public double getYield() {
        return yield;
    }

    public void setYield(double yield) {
        this.yield = yield;
    }

    public UnitValueDTO getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(UnitValueDTO totalVolume) {
        this.totalVolume = totalVolume;
    }

    public UnitValueDTO getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(UnitValueDTO totalWeight) {
        this.totalWeight = totalWeight;
    }

    public UnitValueDTO getTotalMoles() {
        return totalMoles;
    }

    public void setTotalMoles(UnitValueDTO totalMoles) {
        this.totalMoles = totalMoles;
    }

    public String getLastUpdatedType() {
        return lastUpdatedType;
    }

    public void setLastUpdatedType(String lastUpdatedType) {
        this.lastUpdatedType = lastUpdatedType;
    }

    public UnitValueDTO getPrevMolarAmount() {
        return prevMolarAmount;
    }

    public void setPrevMolarAmount(UnitValueDTO prevMolarAmount) {
        this.prevMolarAmount = prevMolarAmount;
    }
}
