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
        PURITY_STRING: function(property, value) {
            return {asString: value};
        },
        PURITY_COMENTS: function(property, value, index) {
            var path = 'data.' + index + '.comments';
            return _.set({}, path, value);
        },
        PURITY_DETERMINED: function(property, value, index) {
            var path = 'data.' + index + '.determinedBy';
            return _.set({}, path, value);
        },
        PURITY_OPERATOR_NAME: function(property, value, index) {
            var path = 'data.' + index + '.operator.name';
            return _.set({}, path, value);
        },
        PURITY_VALUE: function(property, value, index) {
            var path = 'data.' + index + '.value';
            return _.set({}, path, parseInt(value));
        },
        MELTING_POINT: function(property, value) {
            return {asString: value};
        },
        MELTING_POINT_COMMENTS: function(property, value) {
            return {comments: value};
        },
        MELTING_POINT_LOWER: function(property, value) {
            return {lower: value};
        },
        MELTING_POINT_UPPER: function(property, value) {
            return {upper: value};
        },
        RESIDUAL_SOLVENTS_STRING: function(property, value) {
            return {asString: value};
        },
        RESIDUAL_SOLVENTS_COMMENT: function(property, value, index) {
            var path = 'data.' + index + '.comment';
            return _.set({}, path, value);
        },
        RESIDUAL_SOLVENTS_EQ: function(property, value, index) {
            var path = 'data.' + index + '.eq';
            return _.set({}, path, value);
        },
        RESIDUAL_SOLVENTS_RANK: function(property, value, index) {
            var path = 'data.' + index + '.name.rank';
            return _.set({}, path, parseInt(value));
        },
        RESIDUAL_SOLVENTS_NAME: function(property, value, index) {
            var path = 'data.' + index + '.name.name';
            return _.set({}, path, value);
        },
        SOLUBILITY_SOLVENTS_STRING: function(property, value) {
            return {asString: value};
        },
        SOLUBILITY_SOLVENTS_COMMENT: function(property, value, index) {
            var path = 'data.' + index + '.comment';
            return _.set({}, path, value);
        },
        SOLUBILITY_SOLVENTS_TYPE: function(property, value, index) {
            var path = 'data.' + index + '.type.name';
            return _.set({}, path, value);
        },
        SOLUBILITY_SOLVENTS_RANK: function(property, value, index) {
            var path = 'data.' + index + '.solventName.rank';
            return _.set({}, path, parseInt(value));
        },
        SOLUBILITY_SOLVENTS_NAME: function(property, value, index) {
            var path = 'data.' + index + '.solventName.name';
            return _.set({}, path, value);
        },
        SOLUBILITY_SOLVENTS_ENABLE: function(property, value, index) {
            var path = 'data.' + index + '.solventName.enable';
            return _.set({}, path, value);
        },
        SOLUBILITY_SOLVENTS_VALUE_NAME: function(property, value, index) {
            var path = 'data.' + index + '.value.value.name';
            return _.set({}, path, value);
        },
        SOLUBILITY_SOLVENTS_VALUE: function(property, value, index) {
            var path = 'data.' + index + '.value.value';
            return _.set({}, path, value);
        },
        SOLUBILITY_SOLVENTS_VALUE_OPERATOR: function(property, value, index) {
            var path = 'data.' + index + '.value.operator.name';
            return _.set({}, path, value);
        },
        SOLUBILITY_SOLVENTS_VALUE_UNIT: function(property, value, index) {
            var path = 'data.' + index + '.value.unit.name';
            return _.set({}, path, value);
        },
        EXTERNAL_SUPPLIER_STRING: function(property, value) {
            return {asString: value};
        },
        EXTERNAL_SUPPLIER_REG_NUMBER: function(property, value) {
            return {catalogRegistryNumber: value};
        },
        EXTERNAL_SUPPLIER_CODE_AND_NAME: function(property, value) {
            return {codeAndName: {name: value}};
        },
        COMPOUND_PROTECTION: function(property, value) {
            return {name: value};
        },
        HEALTH_HAZARDS_STRING: function(property, value) {
            return {asString: value};
        },
        HEALTH_HAZARDS: function(property, value, index) {
            var path = 'data.' + index;
            return _.set({}, path, value);
        },
        HANDLING_PRECAUTIONS_STRING: function(property, value) {
            return {asString: value};
        },
        HANDLING_PRECAUTIONS: function(property, value, index) {
            var path = 'data.' + index;
            return _.set({}, path, value);
        },
        STORAGE_INSTRUCTIONS_STRING: function(property, value) {
            return {asString: value};
        },
        STORAGE_INSTRUCTIONS: function(property, value, index) {
            var path = 'data.' + index;
            return _.set({}, path, value);
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