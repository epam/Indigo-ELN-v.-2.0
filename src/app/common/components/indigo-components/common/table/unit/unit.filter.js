function unit(unitsConverterService) {
    return function(value, to) {
        if (!value) {
            return value;
        }

        return +unitsConverterService.convert(value).as(to).val();
    };
}

module.exports = unit;

