angular
    .module('indigoeln')
    .factory('sdImportHelperService', sdImportHelperService);

/* @ngInject */
function sdImportHelperService(AppValues) {
    var additionalFormatFunctions = {
        GLOBAL_SALT_CODE: function(property, value) {
            return getItem(AppValues.getSaltCodeValues(), property.subPropName, value);
        },
        GLOBAL_SALT_EQ: function(property, value) {
            return {
                value: parseInt(value, 10),
                entered: false
            };
        },
        STRUCTURE_COMMENT: function(property, value) {
            return value;
        },
        BATCH_COMMENTS: function(property, value) {
            return value;
        },
        BUTCH_TYPE: function(property, value) {
            return value;
        }
    };

    return {
        additionalFormatFunctions: additionalFormatFunctions,
        getWord: getWord,
        formatProperty: formatProperty
    };

    function formatPropertyPath(property, value, index) {
        var parsedValue = property.isNumeric ? parseInt(value, 10) : value;

        return _.set({}, property.path.replace(/<%= index =>/, index), parsedValue);
    }

    function getFormatProperty(property) {
        if (property.path) {
            return formatPropertyPath;
        }

        return additionalFormatFunctions[property.code];
    }

    function formatProperty(property, value, dicts, index) {
        var formatFunc = getFormatProperty(property);
        if (formatFunc) {
            return formatFunc(property, value, index);
        }

        return getWord(property.propName, property.subPropName, value, dicts);
    }

    function getWord(propName, subPropName, value, dicts) {
        var item = _.find(dicts, function(dict) {
            return dict.name === propName;
        });
        if (item) {
            return getItem(item.words, subPropName, value);
        }
    }

    function getItem(list, prop, value) {
        return _.find(list, function(item) {
            return item[prop].toUpperCase() === value.toUpperCase();
        });
    }
}
