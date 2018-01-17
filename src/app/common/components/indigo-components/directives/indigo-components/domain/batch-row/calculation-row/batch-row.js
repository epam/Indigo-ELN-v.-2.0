var BaseBatchRow = require('../base-batch-row');

function BatchRow(props) {
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

BatchRow.prototype = Object.create(BaseBatchRow.prototype);
BatchRow.constructor = BatchRow;

module.exports = BatchRow;
