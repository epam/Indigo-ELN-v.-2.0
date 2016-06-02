package com.epam.indigoeln.web.rest.dto.common;

public class UnitValueDTO {

    private double value;
    private String unit;

    public UnitValueDTO() {
    }

    public UnitValueDTO(double value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnitValueDTO that = (UnitValueDTO) o;

        if (Double.compare(that.value, value) != 0) return false;
        return unit != null ? unit.equals(that.unit) : that.unit == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }
}
