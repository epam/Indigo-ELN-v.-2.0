var BaseBatchRow = require('../base-batch-row');

function BatchRow(props) {
    _.assignWith(this, props, function(defaultValue, valueFromProps) {
        if (valueFromProps && valueFromProps.unit) {
            var obj = {value: valueFromProps.value};
            if (_.has(valueFromProps, 'entered')) {
                obj.entered = valueFromProps.entered;
            }

            if (_.has(valueFromProps, 'readonly')) {
                obj.readonly = valueFromProps.readonly;
            }

            return obj;
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
