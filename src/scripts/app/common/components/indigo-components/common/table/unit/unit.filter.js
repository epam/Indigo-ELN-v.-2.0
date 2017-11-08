function unit(unitsConverter) {
    return function(value, to) {
        if (!value) {
            return value;
        }

        return +unitsConverter.convert(value).as(to).val();
    };
}

module.exports = unit;

