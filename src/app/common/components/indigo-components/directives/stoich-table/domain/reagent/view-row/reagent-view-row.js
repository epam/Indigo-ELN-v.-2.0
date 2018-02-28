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

var ReagentViewField = require('./reagent-view-field');
var calculationHelper = require('../../../../../services/calculation/calculation-helper.service');
var fieldTypes = require('../../../../../services/calculation/field-types');
var mathCalculation = require('../../../../../services/calculation/math-calculation');

function ReagentViewRow(props) {
    var rowProps = getDefaultReagentViewRow();

    if (props && _.isObject(props)) {
        // Assign known properties from given obj
        // This will mutate rowProps object
        setRowProperties(rowProps, props);
    }

    _.defaults(this, rowProps);

    return this;
}

function getOriginalMolWeight(prop) {
    if (prop.molWeight.originalValue) {
        return prop.molWeight.originalValue;
    }
    if (prop.molWeight.baseValue) {
        return prop.molWeight.baseValue;
    }
    if (prop.saltCode && prop.saltCode.weight && prop.saltEq && prop.saltEq.value) {
        return mathCalculation.computeCurrentMolWeightBySalt(
            prop.molWeight.value,
            prop.saltCode.weight,
            prop.saltEq.value
        );
    }

    return prop.molWeight.value;
}

function setLimiting(obj, value) {
    obj.value = _.isObject(value) ? value.value : value;
    obj.readonly = _.isObject(value) ? value.readonly : false;
}

function setFormula(obj, value) {
    obj.value = _.isObject(value) ? value.value : value;
    obj.baseValue = calculationHelper.getBaseFormula(obj.value);
}

function setRxn(obj, customProps) {
    obj.rxnRole.name = customProps.rxnRole.name;

    if (_.has(customProps, fieldTypes.prevRxnRole)) {
        obj.prevRxnRole.name = customProps.prevRxnRole.name;
    } else {
        obj.prevRxnRole.name = obj.rxnRole.name;
    }
}

function setRowProperties(defaultProps, customProps) {
    // Assign known custom properties to default object
    _.forEach(customProps, function(value, key) {
        if (fieldTypes.isId(key)) {
            defaultProps[key] = value;

            return;
        }
        if (fieldTypes.isMolWeight(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].entered = value.entered;
            defaultProps[key].originalValue = getOriginalMolWeight(customProps);

            return;
        }
        if (fieldTypes.isReagentField(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].entered = value.entered;

            return;
        }
        if (fieldTypes.isEq(key) || fieldTypes.isStoicPurity(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].prevValue = value.prevValue ? value.prevValue : value.value;
            defaultProps[key].entered = value.entered;

            return;
        }
        if (fieldTypes.isLimiting(key)) {
            setLimiting(defaultProps[key], value);

            return;
        }
        if (fieldTypes.isFormula(key)) {
            setFormula(defaultProps[key], value);

            return;
        }
        if (fieldTypes.isRxnRole(key)) {
            setRxn(defaultProps, customProps);

            return;
        }
        defaultProps[key] = value;
    });
}

ReagentViewRow.prototype = {
    changesQueue: [],
    clear: clear,
    constructor: ReagentViewRow
};

ReagentViewRow.getDefaultReagentViewRow = getDefaultReagentViewRow;

function clear() {
    _.assign(this, getDefaultReagentViewRow());
}

function getDefaultReagentViewRow() {
    return {
        id: calculationHelper.getId(),
        compoundId: null,
        chemicalName: null,
        fullNbkBatch: null,
        molWeight: {value: 0, originalValue: 0, entered: false},
        weight: new ReagentViewField(0, 'mg'),
        volume: new ReagentViewField(0, 'mL'),
        mol: new ReagentViewField(0, 'mmol'),
        eq: {value: 1, prevValue: 1, entered: false, readonly: false},
        limiting: {value: false, readonly: false},
        rxnRole: {name: 'REACTANT'},
        prevRxnRole: {name: 'REACTANT'},
        density: new ReagentViewField(0, 'g/mL'),
        molarity: new ReagentViewField(0, 'M'),
        stoicPurity: {value: 100, prevValue: 100, entered: false, readonly: false},
        formula: {value: null, baseValue: null},
        saltCode: {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0, readonly: false},
        saltEq: {value: 0, entered: false},
        loadFactor: new ReagentViewField(1, 'mmol/g'),
        hazardComments: null,
        comments: null
    };
}

module.exports = ReagentViewRow;
