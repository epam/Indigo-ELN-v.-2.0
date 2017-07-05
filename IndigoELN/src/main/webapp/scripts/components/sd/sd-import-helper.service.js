angular
    .module('indigoeln')
    .factory('SdImportHelperService', SdImportHelperService);

/* @ngInject */
function SdImportHelperService(AppValues) {
    var additionalFormatFunctions = {
        GLOBAL_SALT_CODE: function(property, value) {
            return getItem(AppValues.getSaltCodeValues(), property.subPropName, value);
        },
        GLOBAL_SALT_EQ: function(property, value) {
            return {
                value: parseInt(value),
                entered: false
            };
        },
        STRUCTURE_COMMENT: function(property, value) {
            return value;
        },
        BATCH_COMMENTS: function(property, value) {
            return value;
        }
    };

    return {
        additionalFormatFunctions: additionalFormatFunctions,
        getWord: getWord,
        formatProperty: formatProperty
    };

    function getFormatProperty(property, value) {
        if (property.path && !_.isNaN(parseInt(value))) {
            return function(_property, _value, index) {
                return _.set({}, _property.path.replace(/<%= index =>/, index), parseInt(_value));
            };
        } else if (property.path) {
            return function(_property, _value, index) {
                return _.set({}, _property.path.replace(/<%= index =>/, index), _value);
            };
        }

        return additionalFormatFunctions[property.code];
    }

    function formatProperty(property, value, dicts, index) {
        var formatFunc = getFormatProperty(property, value);
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
