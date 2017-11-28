var StoichField = require('./stoich-field');
var calculationUtil = require('../calculation/calculation-util');

function StoichRow() {
    _.defaults(this, getDefaultFields());
}

StoichRow.prototype.isSolventRow = isSolventRow;
StoichRow.prototype.isFieldPresent = isFieldPresent;
StoichRow.prototype.areFieldsPresent = areFieldsPresent;
StoichRow.prototype.isLimiting = isLimiting;
StoichRow.prototype.getResetFieldsForDensity = getResetFieldsForDensity;
StoichRow.prototype.updateMolWeight = updateMolWeight;
StoichRow.prototype.updateVolume = updateVolume;
StoichRow.prototype.updateEQ = updateEQ;
StoichRow.prototype.updateMol = updateMol;
StoichRow.prototype.setComputedMolWeight = setComputedMolWeight;
StoichRow.prototype.setComputedVolume = setComputedVolume;
StoichRow.prototype.setComputedWeight = setComputedWeight;
StoichRow.prototype.setComputedMol = setComputedMol;
StoichRow.prototype.setDefaultValues = setDefaultValues;
StoichRow.prototype.setEntered = setEntered;
StoichRow.prototype.setReadonly = setReadonly;
StoichRow.prototype.resetFields = resetFields;
StoichRow.prototype.resetEntered = resetEntered;
StoichRow.prototype.clear = clear;

function updateMolWeight() {
    if (!this.molWeight.value && this.mol.value && this.weight.value) {
        this.resetEntered(['molWeight']);
        this.molWeight.value = calculationUtil.computeMolWeight(this.weight.value, this.mol.value);
    }
}

//TODO: refactor
function updateVolume() {
    if (this.molarity.value) {
        this.resetEntered(['volume']);
        this.volume.value = calculationUtil.computeVolume(this.mol.value, this.molarity.value);

        return;
    }

    if (this.density.value) {
        this.resetEntered(['volume']);
        this.volume.value = calculationUtil.computeVolumeByDensity(this.weight.value, this.density.value);

        return;
    }
}

function updateEQ(limitingRow) {
    if (limitingRow) {
        this.resetEntered(['eq']);
        this.eq.value = calculationUtil.computeEQ(this.mol.value, limitingRow.mol.value);
    }
}

function updateMol(mol, callback) {
    var canUpdateMol = !this.isLimiting() && !this.weight.entered;

    if (canUpdateMol) {

        this.isSolventRow()
            ? this.mol.value = 0
            : this.mol.value = mol;

        callback(this);
    }
}

function setDefaultValues(fields) {
    var self = this;
    var defaultFields = getDefaultFields();

    _.forEach(fields, function(id) {
        self[id] = defaultFields[id];
    });
}

function setEntered(field) {
    this[field].entered = true;
}
//TODO: maybe it useless? just setDefaultValues
function resetEntered(fields) {
    var self = this;

    _.forEach(fields, function(id) {
        self[id].entered = false;
    });
}

function isSolventRow() {
    return this.rxnRole.name === 'SOLVENT';
}

function isFieldPresent(field) {
    return this[field].value;
}

function areFieldsPresent(fields) {
    var self = this;

    return _.every(fields, function(fieldId) {
        return self[fieldId].value;
    });
}

function isLimiting() {
    return this.limiting;
}

function getResetFieldsForDensity() {
    var resetFields = [];
    if (!this.volume.entered) {
        resetFields.push('volume');
    } else if (!this.weight.entered) {
        resetFields.push('weight');
    }

    return resetFields;
}

function setComputedMolWeight(molWeight, callback) {
    this.molWeight.value = molWeight;
    this.resetEntered(['molWeight']);

    if (callback) {
        callback(this);
    }
}

function setComputedVolume(volume, callback) {
    this.volume.value = volume;
    this.resetEntered(['volume']);

    if (callback) {
        callback(this);
    }
}

function setComputedWeight(weight, callback) {
    this.weight.value = weight;
    this.resetEntered(['weight']);

    if (callback) {
        callback(this);
    }
}

function setComputedMol(mol, callback) {
    this.mol.value = mol;
    this.resetEntered(['mol']);

    if (callback) {
        callback(this);
    }
}

function resetFields(fields, callback) {
    this.setDefaultValues(fields);
    //TODO: remove
    // this.resetEntered(fields);

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

function clear() {
    _.assign(this, getDefaultFields());
}

//TODO create object on every call
function getDefaultFields() {
    return {
        compoundId: '',
        chemicalName: '',
        fullNbkBatch: '',
        molWeight: new StoichField(),
        weight: new StoichField(0, 'mg'),
        volume: new StoichField(0, 'mL'),
        mol: new StoichField(0, 'mmol'),
        eq: new StoichField(1),
        limiting: false,
        rxnRole: {name: 'REACTANT', entered: false},
        density: new StoichField(0, 'g/mL'),
        molarity: new StoichField(0, 'M'),
        stoicPurity: new StoichField(100),
        formula: '',
        saltCode: {name: '00 - Parent Structure', value: '0', regValue: '00', entered: false, weight: 0},
        saltEq: new StoichField(),
        loadFactor: '',
        hazardComments: '',
        comments: ''
    };
}

module.exports = StoichRow;
