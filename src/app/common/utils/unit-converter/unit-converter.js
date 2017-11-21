function UnitConverter(value, unit) {
    this.value = value;
    if (unit) {
        this.currentUnit = unit;
    }
}

UnitConverter.prototype = {
    table: {},
    as: function(targetUnit) {
        this.targetUnit = targetUnit;

        return this;
    },
    is: function(currentUnit) {
        this.currentUnit = currentUnit;

        return this;
    },
    val: function() {
        if (!this.currentUnit && this.targetUnit && this.table[this.targetUnit]) {
            this.currentUnit = this.table[this.targetUnit].indigoBase;
        }
        if (!this.targetUnit && this.currentUnit && this.table[this.currentUnit]) {
            this.targetUnit = this.table[this.currentUnit].indigoBase;
        }
        if (!this.currentUnit || !this.targetUnit) {
            return this.value;
        }
        // first, convert from the current value to the base unit
        var target = this.table[this.targetUnit];
        var current = this.table[this.currentUnit];
        if (target.base !== current.base) {
            throw new Error(
                'Incompatible units; cannot convert from "' + this.currentUnit + '" to "' + this.targetUnit + '"'
            );
        }

        return (this.value / target.multiplier) * current.multiplier;
    },
    toBase: function(unit) {
        return this.table[unit] ? this.table[unit].base : unit;
    },
    toString: function() {
        return this.val() + ' ' + this.targetUnit;
    },
    debug: function() {
        return this.value + ' ' + this.currentUnit + ' is ' + this.val() + ' ' + this.targetUnit;
    },
    addUnit: function(baseUnit, actualUnit, multiplier, indigoBase) {
        this.table[actualUnit] = {
            base: baseUnit,
            actual: actualUnit,
            multiplier: multiplier,
            indigoBase: indigoBase
        };
    }
};

module.exports = UnitConverter;
