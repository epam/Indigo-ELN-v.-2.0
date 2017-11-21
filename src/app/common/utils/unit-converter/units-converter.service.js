var units = require('./units-in-si-system.json');

/* @ngInject */
function unitsConverter() {
    var prefixes =
        ['Y', 'Z', 'E', 'P', 'T', 'G', 'M', 'k', 'h', 'da', '', 'd', 'c', 'm', 'u', 'n', 'p', 'f', 'a', 'z', 'y'];
    var factors = [24, 21, 18, 15, 12, 9, 6, 3, 2, 1, 0, -1, -2, -3, -6, -9, -12, -15, -18, -21, -24];

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

    function initUnits() {
        _.forEach(units, function(unit) {
            var base = unit.si;
            var indigoBase = unit.indigoBase;
            _.forEach(prefixes, function(prefix, i) {
                UnitConverter.prototype.addUnit(base, prefixes[i] + base, Math.pow(10, factors[i]), indigoBase);
            });
        });

        // we use the SI gram unit as the base; this allows
        // us to convert between SI and English units
        UnitConverter.prototype.addUnit('g', 'ounce', 28.3495231);
        UnitConverter.prototype.addUnit('g', 'oz', 28.3495231);
        UnitConverter.prototype.addUnit('g', 'pound', 453.59237);
        UnitConverter.prototype.addUnit('g', 'lb', 453.59237);
    }

    initUnits();

    return {
        table: UnitConverter.prototype.table,
        convert: function(value, unit) {
            /* eslint new-cap: "off"*/
            return new UnitConverter(value, unit);
        }
    };
}

module.exports = unitsConverter;
