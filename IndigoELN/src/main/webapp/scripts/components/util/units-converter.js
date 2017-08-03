(function() {
    angular
        .module('indigoeln')
        .factory('UnitsConverter', unitsConverter);

    function unitsConverter() {
        var table = {};
        var prefixes = ['Y', 'Z', 'E', 'P', 'T', 'G', 'M', 'k', 'h', 'da', '', 'd', 'c', 'm', 'u', 'n', 'p', 'f', 'a', 'z', 'y'];
        var factors = [24, 21, 18, 15, 12, 9, 6, 3, 2, 1, 0, -1, -2, -3, -6, -9, -12, -15, -18, -21, -24];
        // SI units only, that follow the mg/kg/dg/cg type of format
        var units = [
            {
                si: 'g', indigoBase: 'mg'
            }, {
                si: 'b', indigoBase: 'b'
            }, {
                si: 'l', indigoBase: 'ml'
            }, {
                si: 'm',
                indigoBase: 'm'
            }, {
                si: 'mol', indigoBase: 'mmol'
            }, {
                si: 'M', indigoBase: 'M'
            }];

        function unitConverter(value, unit) {
            this.value = value;
            if (unit) {
                this.currentUnit = unit;
            }
        }

        unitConverter.prototype.as = function(targetUnit) {
            this.targetUnit = targetUnit;

            return this;
        };

        unitConverter.prototype.is = function(currentUnit) {
            this.currentUnit = currentUnit;

            return this;
        };

        unitConverter.prototype.val = function() {
            if (!this.currentUnit && this.targetUnit && table[this.targetUnit]) {
                this.currentUnit = table[this.targetUnit].indigoBase;
            }
            if (!this.targetUnit && this.currentUnit && table[this.currentUnit]) {
                this.targetUnit = table[this.currentUnit].indigoBase;
            }
            if (!this.currentUnit || !this.targetUnit) {
                return this.value;
            }
            // first, convert from the current value to the base unit
            var target = table[this.targetUnit];
            var current = table[this.currentUnit];
            if (target.base !== current.base) {
                throw new Error('Incompatible units; cannot convert from "' + this.currentUnit + '" to "' + this.targetUnit + '"');
            }

            return this.value * (current.multiplier / target.multiplier);
        };
        unitConverter.prototype.toString = function() {
            return this.val() + ' ' + this.targetUnit;
        };
        unitConverter.prototype.debug = function() {
            return this.value + ' ' + this.currentUnit + ' is ' + this.val() + ' ' + this.targetUnit;
        };
        unitConverter.addUnit = function(baseUnit, actualUnit, multiplier, indigoBase) {
            table[actualUnit] = {
                base: baseUnit, actual: actualUnit, multiplier: multiplier, indigoBase: indigoBase
            };
        };

        _.each(units, function(unit) {
            var base = unit.si;
            var indigoBase = unit.indigoBase;
            _.each(prefixes, function(prefix, i) {
                unitConverter.addUnit(base, prefixes[i] + base, Math.pow(10, factors[i]), indigoBase);
            });
        });


        // we use the SI gram unit as the base; this allows
        // us to convert between SI and English units
        unitConverter.addUnit('g', 'ounce', 28.3495231);
        unitConverter.addUnit('g', 'oz', 28.3495231);
        unitConverter.addUnit('g', 'pound', 453.59237);
        unitConverter.addUnit('g', 'lb', 453.59237);

        unitConverter.prototype.toBase = function(unit) {
            return table[unit] ? table[unit].base : unit;
        };

        return {
            convert: function(value, unit) {
                return new unitConverter(value, unit);
            }
        };
    }
})();
