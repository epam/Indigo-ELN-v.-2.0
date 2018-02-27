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
