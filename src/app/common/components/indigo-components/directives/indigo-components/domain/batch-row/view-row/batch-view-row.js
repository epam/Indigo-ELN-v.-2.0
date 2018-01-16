var BatchViewField = require('./batch-view-field');
// TODO: shouldn't be in stoich table
var fieldTypes = require('../../../../stoich-table/domain/field-types');
var calculationHelper = require('../../../../../services/calculation/calculation-helper.service');

function BatchViewRow(props) {
    var rowProps = getDefaultBatchViewRow();

    if (props && _.isObject(props)) {
        // Assign known properties from given obj
        // This will mutate rowProps object
        setRowProperties(rowProps, props);
    }

    _.defaults(this, rowProps);

    return this;
}

BatchViewRow.prototype = {
    setTheoMoles: setTheoMoles,
    setMolWeight: setMolWeight,
    setTheoWeight: setTheoWeight,
    setFormula: setFormula,
    constructor: BatchViewRow
};

function setRowProperties(defaultProps, customProps) {
    // Assign known custom properties to default object
    _.forEach(customProps, function(value, key) {
        if (fieldTypes.isId(key)) {
            defaultProps[key] = value;
        } else if (fieldTypes.isMolWeight(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].baseValue = value.value;
            defaultProps[key].entered = value.entered;
        } else if (fieldTypes.isFormula(key)) {
            defaultProps[key].value = _.isObject(value) ? value.value : value;
            defaultProps[key].baseValue = _.isObject(value) ? value.baseValue : value;
        }
    });

    // Replace default values and add missing from given customProps obj
    _.assignWith(defaultProps, customProps, function(defaultValue, valueFromJson) {
        return _.isNil(defaultValue)
            ? valueFromJson
            : defaultValue;
    });
}

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

function getDefaultBatchViewRow() {
    return {
        id: calculationHelper.getId(),
        nbkBatch: null,
        fullNbkBatch: null,
        fullNbkImmutablePart: null,
        virtualCompoundId: null,
        conversationalBatchNumber: null,
        registrationDate: null,
        registrationStatus: null,
        molWeight: {value: 0, baseValue: 0, entered: false},
        formula: {value: null, baseValue: null},
        stereoisomer: null,
        structureComments: null,
        saltCode: {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0, readonly: false},
        saltEq: {value: 0},
        structure: null,
        precursors: null,
        theoWeight: {value: 0, unit: 'mg'},
        theoMoles: {value: 0, unit: 'mmol'},
        totalWeight: new BatchViewField(0, 'mg'),
        totalMoles: new BatchViewField(0, 'mmol'),
        totalVolume: new BatchViewField(0, 'mL'),
        yield: 0,
        compoundState: null,
        purity: null,
        meltingPoint: null,
        source: null,
        sourceDetail: null,
        externalSupplier: null,
        healthHazards: null,
        compoundProtection: null,
        residualSolvents: null,
        solubility: null,
        storageInstructions: null,
        handlingPrecautions: null,
        comments: null,
        batchType: null
    };
}

module.exports = BatchViewRow;
