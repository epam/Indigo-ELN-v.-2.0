var fieldTypes = require('../../../../../services/calculation/field-types');
var calculationHelper = require('../../../../../services/calculation/calculation-helper.service');

function ProductViewRow(props) {
    var rowProps = getDefaultProductViewRow();

    if (props && _.isObject(props)) {
        // Assign known properties from given obj
        // This will mutate rowProps object
        setRowProperties(rowProps, props);
    }

    _.defaults(this, rowProps);

    return this;
}

function setRowProperties(defaultProps, customProps) {
    // Assign known custom properties to default object
    _.forEach(customProps, function(value, key) {
        if (fieldTypes.isId(key)) {
            defaultProps[key] = value;
        } else if (fieldTypes.isMolWeight(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].baseValue = value.value;
        } else if (fieldTypes.isFormula(key)) {
            defaultProps[key].value = _.isObject(value) ? value.value : value;
            defaultProps[key].baseValue = _.isObject(value) ? value.baseValue : value;
        } else if (fieldTypes.isEq(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].prevValue = value.prevValue ? value.prevValue : value.value;
            defaultProps[key].entered = value.entered;
        } else {
            defaultProps[key] = value;
        }
    });
}

function getDefaultProductViewRow() {
    return {
        id: calculationHelper.getId(),
        chemicalName: null,
        formula: {value: null, baseValue: null},
        molWeight: {value: 0, baseValue: 0},
        exactMass: null,
        theoWeight: {value: 0, unit: 'mg'},
        theoMoles: {value: 0, unit: 'mmol'},
        saltCode: {name: '00 - Parent Structure', value: '0', regValue: '00', weight: 0},
        saltEq: {value: 0},
        hazardComments: null,
        eq: {value: 1, prevValue: 1, entered: false}
    };
}

module.exports = ProductViewRow;
