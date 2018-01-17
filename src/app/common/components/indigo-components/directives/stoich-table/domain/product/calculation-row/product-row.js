function ProductRow(props) {
    _.assignWith(this, props, function(defaultValue, valueFromProps) {
        if (valueFromProps && valueFromProps.unit) {
            return _.omit(valueFromProps, ['unit']);
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
