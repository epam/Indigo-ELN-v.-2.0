/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
