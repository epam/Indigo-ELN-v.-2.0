var fieldTypes = require('../field-types');

function ProductRow(props) {
    var rowProps = getDefaultProductRow();

    if (props && _.isObject(props)) {
        // Assign known properties from given obj
        // This will mutate rowProps object
        setRowProperties(rowProps, props);
    }

    _.defaults(this, rowProps);

    return this;
}

ProductRow.prototype = {
    updateTheoMoles: updateTheoMoles,
    updateTheoWeight: updateTheoWeight,
    updateMolWeight: updateMolWeight,
    constructor: ProductRow
};

function setRowProperties(defaultProps, customProps) {
    // Assign known custom properties to default object
    _.forEach(customProps, function(value, key) {
        if (fieldTypes.isMolWeight(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].baseValue = value.value;
        } else if (fieldTypes.isEq(key)) {
            defaultProps[key].value = value.value;
            defaultProps[key].prevValue = value.prevValue ? value.prevValue : value.value;
            defaultProps[key].entered = value.entered;
        }
    });

    // Replace default values and add missing from given customProps obj
    _.assignWith(defaultProps, customProps, function(defaultValue, valueFromJson) {
        return _.isNil(defaultValue)
            ? valueFromJson
            : defaultValue;
    });
}

function updateTheoMoles(value) {
    this.theoMoles.value = value;

    return this;
}

function updateTheoWeight(value) {
    this.theoWeight.value = value;

    return this;
}

function updateMolWeight(value) {
    this.molWeight.value = value;

    return this;
}

function getDefaultProductRow() {
    return {
        chemicalName: null,
        formula: null,
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

module.exports = ProductRow;
