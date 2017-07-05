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
        PURITY_STRING: function(property, value) {
            return _.set({}, property.path, value);
        },
        PURITY_COMENTS: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        PURITY_DETERMINED: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        PURITY_OPERATOR_NAME: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        PURITY_VALUE: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), parseInt(value));
        },
        MELTING_POINT: function(property, value) {
            return _.set({}, property.path, value);
        },
        MELTING_POINT_COMMENTS: function(property, value) {
            return _.set({}, property.path, value);
        },
        MELTING_POINT_LOWER: function(property, value) {
            return _.set({}, property.path, parseInt(value));
        },
        MELTING_POINT_UPPER: function(property, value) {
            return _.set({}, property.path, parseInt(value));
        },
        RESIDUAL_SOLVENTS_STRING: function(property, value) {
            return _.set({}, property.path, value);
        },
        RESIDUAL_SOLVENTS_COMMENT: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        RESIDUAL_SOLVENTS_EQ: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        RESIDUAL_SOLVENTS_RANK: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), parseInt(value));
        },
        RESIDUAL_SOLVENTS_NAME: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        SOLUBILITY_SOLVENTS_STRING: function(property, value) {
            return _.set({}, property.path, value);
        },
        SOLUBILITY_SOLVENTS_COMMENT: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        SOLUBILITY_SOLVENTS_TYPE: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        SOLUBILITY_SOLVENTS_RANK: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), parseInt(value));
        },
        SOLUBILITY_SOLVENTS_NAME: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        SOLUBILITY_SOLVENTS_ENABLE: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        SOLUBILITY_SOLVENTS_VALUE_NAME: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        SOLUBILITY_SOLVENTS_VALUE: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        SOLUBILITY_SOLVENTS_VALUE_OPERATOR: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        SOLUBILITY_SOLVENTS_VALUE_UNIT: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        EXTERNAL_SUPPLIER_STRING: function(property, value) {
            return _.set({}, property.path, value);
        },
        EXTERNAL_SUPPLIER_REG_NUMBER: function(property, value) {
            return _.set({}, property.path, value);
        },
        EXTERNAL_SUPPLIER_CODE_AND_NAME: function(property, value) {
            return _.set({}, property.path, value);
        },
        COMPOUND_PROTECTION: function(property, value) {
            return _.set({}, property.path, value);
        },
        HEALTH_HAZARDS_STRING: function(property, value) {
            return _.set({}, property.path, value);
        },
        HEALTH_HAZARDS: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        HANDLING_PRECAUTIONS_STRING: function(property, value) {
            return _.set({}, property.path, value);
        },
        HANDLING_PRECAUTIONS: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
        },
        STORAGE_INSTRUCTIONS_STRING: function(property, value) {
            return _.set({}, property.path, value);
        },
        STORAGE_INSTRUCTIONS: function(property, value, index) {
            return _.set({}, property.path.replace(/index/, index), value);
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

    function getFormatProperty(property) {
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