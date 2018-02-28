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

function ProductRow(props) {
    _.assignWith(this, props, function(defaultValue, valueFromProps) {
        if (valueFromProps && valueFromProps.unit) {
            return {value: valueFromProps.value};
        }

        return _.isObject(valueFromProps)
            ? _.assign({}, valueFromProps)
            : valueFromProps;
    });

    return this;
}

ProductRow.prototype = {
    setTheoMoles: setTheoMoles,
    setTheoWeight: setTheoWeight,
    setMolWeight: setMolWeight,
    setFormula: setFormula,
    constructor: ProductRow
};

function setTheoMoles(value) {
    this.theoMoles.value = value;
}

function setTheoWeight(value) {
    this.theoWeight.value = value;
}

function setMolWeight(value) {
    this.molWeight.value = value;
}

function setFormula(value) {
    this.formula.value = value;
}

module.exports = ProductRow;
