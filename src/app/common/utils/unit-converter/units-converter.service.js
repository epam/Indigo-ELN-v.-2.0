var units = require('./units-in-si-system.json');
var UnitConverter = require('./unit-converter');

/* @ngInject */
function unitsConverter() {
    var prefixes =
        ['Y', 'Z', 'E', 'P', 'T', 'G', 'M', 'k', 'h', 'da', '', 'd', 'c', 'm', 'u', 'n', 'p', 'f', 'a', 'z', 'y'];
    var factors = [24, 21, 18, 15, 12, 9, 6, 3, 2, 1, 0, -1, -2, -3, -6, -9, -12, -15, -18, -21, -24];

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
