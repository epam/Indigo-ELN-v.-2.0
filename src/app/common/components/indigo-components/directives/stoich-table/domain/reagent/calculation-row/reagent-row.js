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

var ReagentViewRow = require('../view-row/reagent-view-row');
var fieldTypes = require('../../../../../services/calculation/field-types');
var mathCalculation = require('../../../../../services/calculation/math-calculation');

function ReagentRow(props) {
    _.assignWith(this, props, function(defaultValue, valueFromProps) {
        if (valueFromProps && valueFromProps.unit) {
            return {
                value: valueFromProps.value,
                entered: valueFromProps.entered,
                readonly: valueFromProps.readonly
            };
        }

        return _.isObject(valueFromProps)
            ? _.assign({}, valueFromProps)
            : valueFromProps;
    });

    return this;
}

ReagentRow.prototype = {
    isSolventRow: isSolventRow,
    isLimiting: isLimiting,
    getResetFieldForDensity: getResetFieldForDensity,
    getResetFieldsForSolvent: getResetFieldsForSolvent,
    getResetFieldForMolarity: getResetFieldForMolarity,
    getResetFieldsForMol: getResetFieldsForMol,
    getResetFieldsForWeight: getResetFieldsForWeight,
    setMolWeight: setMolWeight,
    updateVolume: updateVolume,
    updateEq: updateEq,
    setComputedMolWeight: setComputedMolWeight,
    setComputedVolume: setComputedVolume,
    setComputedWeight: setComputedWeight,
    setComputedMol: setComputedMol,
    setDefaultValues: setDefaultValues,
    setEntered: setEntered,
    setReadonly: setReadonly,
    setFormula: setFormula,
    resetFields: resetFields,
    resetEntered: resetEntered,
    isMolWeightPresent: isMolWeightPresent,
    isMolPresent: isMolPresent,
    isWeightPresent: isWeightPresent,
    isVolumePresent: isVolumePresent,
    isMolarityPresent: isMolarityPresent,
    isDensityPresent: isDensityPresent,
    isPurityPresent: isPurityPresent,
    isWeightManuallyEntered: isWeightManuallyEntered,
    isMolManuallyEntered: isMolManuallyEntered,
    isEqManuallyEntered: isEqManuallyEntered,
    isVolumeManuallyEntered: isVolumeManuallyEntered
};

ReagentRow.prototype.constructor = ReagentRow;

function setMolWeight() {
    if (!this.molWeight.value && this.mol.value && this.weight.value) {
        this.resetEntered([fieldTypes.molWeight]);
        this.molWeight.value = mathCalculation.computeMolWeight(this.weight.value, this.mol.value);
        this.molWeight.originalValue = this.molWeight.value;
    }
}

// TODO: refactor
function updateVolume() {
    var areMolarityAndMolPreset = this.isMolarityPresent() && this.isMolPresent();
    var areDensityAndWeightPresent = this.isDensityPresent() && this.isWeightPresent();
    var isManuallyEntered = this.isWeightManuallyEntered() || this.isMolManuallyEntered() || this.isEqManuallyEntered();

    if (areMolarityAndMolPreset) {
        this.volume.value = mathCalculation.computeVolumeByMolarity(this.mol.value, this.molarity.value);
    } else if (areDensityAndWeightPresent) {
        this.volume.value = mathCalculation.computeVolumeByDensity(this.weight.value, this.density.value);
    }

    if (isManuallyEntered && (areMolarityAndMolPreset || areDensityAndWeightPresent)) {
        this.resetEntered([fieldTypes.volume]);
    }
}

function updateEq(limitingRow) {
    var isArgsExist = limitingRow && this.mol.value && limitingRow.mol.value;
    var canUpdateEq = this.weight.entered || this.mol.entered || this.molarity.entered || this.density.entered;

    if (isArgsExist && canUpdateEq) {
        this.resetEntered([fieldTypes.eq]);
        this.eq.value =
            mathCalculation.computeEq(this.mol.value, limitingRow.eq.value, limitingRow.mol.value);
        this.eq.prevValue = this.eq.value;
    }
}

function setDefaultValues(fields) {
    var self = this;
    var defaultFields = ReagentViewRow.getDefaultReagentViewRow();

    _.forEach(fields, function(id) {
        self[id] = defaultFields[id];
    });
}

function setEntered(field) {
    this[field].entered = true;
}

// TODO: maybe it useless? just setDefaultValues
function resetEntered(fields) {
    var self = this;

    _.forEach(fields, function(id) {
        self[id].entered = false;
    });
}

function isSolventRow() {
    return this.rxnRole.name === 'SOLVENT';
}

function isLimiting() {
    return this.limiting.value;
}

function getResetFieldForDensity() {
    if (!this.volume.entered) {
        return fieldTypes.volume;
    }

    if (!this.weight.entered) {
        return fieldTypes.weight;
    }
}

function getResetFieldForMolarity() {
    if (!this.volume.entered) {
        return fieldTypes.volume;
    }

    if (!this.mol.entered) {
        return fieldTypes.mol;
    }
}

function getResetFieldsForWeight() {
    var fields = [fieldTypes.mol, fieldTypes.eq, fieldTypes.weight];

    if (!this.volume.entered) {
        fields.push(fieldTypes.volume);
    }

    return fields;
}

function getResetFieldsForMol() {
    var fields = [fieldTypes.weight, fieldTypes.eq, fieldTypes.mol];

    if (!this.volume.entered) {
        fields.push(fieldTypes.volume);
    }

    return fields;
}

function setComputedMolWeight(molWeight, originalValue, callback) {
    this.molWeight.value = molWeight;
    this.resetEntered([fieldTypes.molWeight]);

    if (callback) {
        callback(this);
    }

    this.molWeight.originalValue = originalValue;
}

function setComputedVolume(volume, callback) {
    this.volume.value = volume;
    this.resetEntered([fieldTypes.volume]);

    if (callback) {
        callback(this);
    }
}

function setComputedWeight(weight, callback) {
    this.weight.value = weight;
    this.resetEntered([fieldTypes.weight]);

    if (callback) {
        callback(this);
    }
}

function setComputedMol(mol, callback) {
    // TODO: set limiting true if limitingRow doesn't exist
    this.mol.value = this.isSolventRow() ? 0 : mol;
    this.resetEntered([fieldTypes.mol]);

    if (callback) {
        callback(this);
    }
}

function resetFields(fields, callback) {
    this.setDefaultValues(fields);

    if (callback) {
        callback(this);
    }
}

function setReadonly(fields, isReadonly) {
    var self = this;

    _.forEach(fields, function(id) {
        self[id].readonly = isReadonly;
    });
}

function setFormula(value) {
    this.formula.value = value;
}

function getResetFieldsForSolvent() {
    return [
        fieldTypes.weight,
        fieldTypes.mol,
        fieldTypes.eq,
        fieldTypes.limiting,
        fieldTypes.molarity,
        fieldTypes.density,
        fieldTypes.stoicPurity,
        fieldTypes.saltCode,
        fieldTypes.saltEq,
        fieldTypes.loadFactor
    ];
}

function isMolPresent() {
    return this.mol.value;
}

function isWeightPresent() {
    return this.weight.value;
}

function isMolWeightPresent() {
    return this.molWeight.value;
}

function isVolumePresent() {
    return this.volume.value;
}

function isMolarityPresent() {
    return this.molarity.value;
}

function isDensityPresent() {
    return this.density.value;
}

function isPurityPresent() {
    return this.stoicPurity.value;
}

function isWeightManuallyEntered() {
    return this.weight.entered;
}

function isMolManuallyEntered() {
    return this.mol.entered;
}

function isEqManuallyEntered() {
    return this.eq.entered;
}

function isVolumeManuallyEntered() {
    return this.volume.entered;
}

module.exports = ReagentRow;
