var StoichField = require('./stoich-field');
var calculationUtil = require('../calculation/calculation-util');

function StoichRow() {
    _.defaults(this, getDefaultFields());
}

StoichRow.prototype.isSolventRow = isSolventRow;
StoichRow.prototype.isValuePresent = isValuePresent;
StoichRow.prototype.areValuesPresent = areValuesPresent;
StoichRow.prototype.isLimiting = isLimiting;
StoichRow.prototype.getResetFieldForDensity = getResetFieldForDensity;
StoichRow.prototype.getResetFieldsForSolvent = getResetFieldsForSolvent;
StoichRow.prototype.getResetFieldForMolarity = getResetFieldForMolarity;
StoichRow.prototype.getResetFieldsForMol = getResetFieldsForMol;
StoichRow.prototype.getResetFieldsForWeight = getResetFieldsForWeight;
StoichRow.prototype.updateMolWeight = updateMolWeight;
StoichRow.prototype.updateVolume = updateVolume;
StoichRow.prototype.updateEq = updateEq;
StoichRow.prototype.updateEqDependingOnLimitingEq = updateEqDependingOnLimitingEq;
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
    if (this.molarity.value && this.mol.value) {
        this.volume.value = calculationUtil.computeVolumeByMolarity(this.mol.value, this.molarity.value);
    } else if (this.density.value && this.weight.value) {
        this.volume.value = calculationUtil.computeVolumeByDensity(this.weight.value, this.density.value);
    }

    if (this.weight.entered || this.mol.entered) {
        this.resetEntered(['volume']);
    }
}

function updateEq(limitingRow) {
    var isArgsExist = limitingRow && this.mol.value && limitingRow.mol.value;
    var canUpdateEq = this.weight.entered || this.mol.entered || this.molarity.entered;

    if (isArgsExist && canUpdateEq) {
        this.resetEntered(['eq']);
        this.eq.value = calculationUtil.computeEq(this.mol.value, limitingRow.mol.value);
        this.eq.prevValue = this.eq.value;
    }
}

function updateEqDependingOnLimitingEq(limitingRow) {
    this.updateEq(limitingRow);
    this.eq.value = calculationUtil.multiply(this.eq.value, limitingRow.eq.value);
    this.eq.prevValue = this.eq.value;
}

function updateMol(mol, callback) {
    this.isSolventRow()
        ? this.mol.value = 0
        : this.mol.value = mol;

    callback(this);
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

function isValuePresent(field) {
    return this[field].value;
}

function areValuesPresent(fields) {
    var self = this;

    return _.every(fields, function(fieldId) {
        return self[fieldId].value;
    });
}

function isLimiting() {
    return this.limiting;
}

function getResetFieldForDensity() {
    if (!this.volume.entered) {
        return 'volume';
    }

    if (!this.weight.entered) {
        return 'weight';
    }
}

function getResetFieldForMolarity() {
    if (!this.volume.entered) {
        return 'volume';
    }

    if (!this.mol.entered) {
        return 'mol';
    }
}

function getResetFieldsForWeight() {
    var resetFields = ['mol', 'eq'];
    if (!this.volume.entered) {
        resetFields.push('volume');
    }

    return resetFields;
}

function getResetFieldsForMol() {
    var resetFields = ['weight', 'eq'];
    if (!this.volume.entered) {
        resetFields.push('volume');
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
    //TODO: set limiting true if limitingRow doesn't exist
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

function getResetFieldsForSolvent() {
    return ['weight', 'mol', 'eq', 'molarity', 'density', 'stoicPurity', 'saltCode', 'saltEq'];
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
        eq: {value: 1, prevValue: 1, entered: false},
        limiting: false,
        rxnRole: {name: 'REACTANT', entered: false},
        density: new StoichField(0, 'g/mL'),
        molarity: new StoichField(0, 'M'),
        stoicPurity: new StoichField(100),
        formula: '',
        saltCode: {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0},
        saltEq: {value: 0},
        loadFactor: '',
        hazardComments: '',
        comments: ''
    };
}

module.exports = StoichRow;
