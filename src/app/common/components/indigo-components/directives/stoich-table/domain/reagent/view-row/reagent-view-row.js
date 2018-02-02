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

function getBaseFormula(formula) {
    if (!formula) {
        return formula;
    }
    var extendedFormulaIndex = formula.indexOf('*');
    if (extendedFormulaIndex !== -1) {
        return formula.substring(0, extendedFormulaIndex).trim();
    }

    return formula;
}

function setRowProperties(defaultProps, customProps) {
    // Assign known custom properties to default object
    _.forEach(customProps, function(value, key) {
        if (fieldTypes.isId(key)) {
            defaultProps[key] = value;
        } else if (fieldTypes.isMolWeight(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].entered = value.entered;
            defaultProps[key].originalValue = getOriginalMolWeight(customProps);
        } else if (fieldTypes.isReagentField(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].entered = value.entered;
        } else if (fieldTypes.isEq(key) || fieldTypes.isStoicPurity(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].prevValue = value.prevValue ? value.prevValue : value.value;
            defaultProps[key].entered = value.entered;
        } else if (fieldTypes.isLimiting(key)) {
            defaultProps[key].value = _.isObject(value) ? value.value : value;
            defaultProps[key].readonly = _.isObject(value) ? value.readonly : false;
        } else if (fieldTypes.isFormula(key)) {
            defaultProps[key].value = _.isObject(value) ? value.value : value;
            defaultProps[key].baseValue = getBaseFormula(defaultProps[key].value);
        } else if (fieldTypes.isRxnRole(key)) {
            defaultProps[key].name = value.name;

            if (_.has(customProps, fieldTypes.prevRxnRole)) {
                defaultProps.prevRxnRole.name = customProps.prevRxnRole.name;
            } else {
                defaultProps.prevRxnRole.name = defaultProps[key].name;
            }
        } else {
            defaultProps[key] = value;
        }
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
        // TODO: rename to purity
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
