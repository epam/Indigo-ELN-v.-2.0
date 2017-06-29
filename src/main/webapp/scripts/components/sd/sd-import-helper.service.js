angular
    .module('indigoeln')
    .factory('SdImportHelperService', SdImportHelperService);

/* @ngInject */
function SdImportHelperService(AppValues) {

    var additionalFormatFunctions = {
        GLOBAL_SALT_CODE: function (property, value) {
            return getItem(AppValues.getSaltCodeValues(), property.subPropName, value);
        },
        GLOBAL_SALT_EQ: function (property, value) {
            return {
                value: parseInt(value),
                entered: false
            };
        },
        PURITY_STRING: function (property, value) {
            return {asString: value};
        },
        PURITY_0_COMENTS: function (property, value) {
            return {data: {0: {comments: value}}};
        },
        PURITY_0_DETERMINED: function (property, value) {
            return {data: {0: {determinedBy: value}}};
        },
        PURITY_0_OPERATOR_NAME: function (property, value) {
            return {data: {0: {operator: {name: value}}}};
        },
        PURITY_0_VALUE: function (property, value) {
            return {data: {0: {value: parseInt(value)}}};
        },
        PURITY_1_COMENTS: function (property, value) {
            return {data: {1: {comments: value}}};
        },
        PURITY_1_DETERMINED: function (property, value) {
            return {data: {1: {determinedBy: value}}};
        },
        PURITY_1_OPERATOR_NAME: function (property, value) {
            return {data: {1: {operator: {name: value}}}};
        },
        PURITY_1_VALUE: function (property, value) {
            return {data: {1: {value: parseInt(value)}}};
        },
        PURITY_2_COMENTS: function (property, value) {
            return {data: {2: {comments: value}}};
        },
        PURITY_2_DETERMINED: function (property, value) {
            return {data: {2: {determinedBy: value}}};
        },
        PURITY_2_OPERATOR_NAME: function (property, value) {
            return {data: {2: {operator: {name: value}}}};
        },
        PURITY_2_VALUE: function (property, value) {
            return {data: {2: {value: parseInt(value)}}};
        },
        PURITY_3_COMENTS: function (property, value) {
            return {data: {3: {comments: value}}};
        },
        PURITY_3_DETERMINED: function (property, value) {
            return {data: {3: {determinedBy: value}}};
        },
        PURITY_3_OPERATOR_NAME: function (property, value) {
            return {data: {3: {operator: {name: value}}}};
        },
        PURITY_3_VALUE: function (property, value) {
            return {data: {3: {value: parseInt(value)}}};
        },
        PURITY_4_COMENTS: function (property, value) {
            return {data: {4: {comments: value}}};
        },
        PURITY_4_DETERMINED: function (property, value) {
            return {data: {4: {determinedBy: value}}};
        },
        PURITY_4_OPERATOR_NAME: function (property, value) {
            return {data: {4: {operator: {name: value}}}};
        },
        PURITY_4_VALUE: function (property, value) {
            return {data: {4: {value: parseInt(value)}}};
        },
        MELTING_POINT: function (property, value) {
            return {asString: value};
        },
        MELTING_POINT_COMMENTS: function (property, value) {
            return {comments: value};
        },
        MELTING_POINT_LOWER: function (property, value) {
            return {lower: value};
        },
        MELTING_POINT_UPPER: function (property, value) {
            return {upper: value};
        },
        RESIDUAL_SOLVENTS_STRING: function (property, value) {
            return {asString: value};
        },
        RESIDUAL_SOLVENTS_0_COMMENT: function (property, value) {
            return {data: {0: {comment: value}}};
        },
        RESIDUAL_SOLVENTS_0_EQ: function (property, value) {
            return {data: {0: {eq: value}}};
        },
        RESIDUAL_SOLVENTS_0_RANK: function (property, value) {
            return {data: {0: {name: {rank: parseInt(value)}}}};
        },
        RESIDUAL_SOLVENTS_0_NAME: function (property, value) {
            return {data: {0: {name: {name: value}}}};
        },
        RESIDUAL_SOLVENTS_1_COMMENT: function (property, value) {
            return {data: {1: {comment: value}}};
        },
        RESIDUAL_SOLVENTS_1_EQ: function (property, value) {
            return {data: {1: {eq: value}}};
        },
        RESIDUAL_SOLVENTS_1_RANK: function (property, value) {
            return {data: {1: {name: {rank: parseInt(value)}}}};
        },
        RESIDUAL_SOLVENTS_1_NAME: function (property, value) {
            return {data: {1: {name: {name: value}}}};
        },
        RESIDUAL_SOLVENTS_2_COMMENT: function (property, value) {
            return {data: {2: {comment: value}}};
        },
        RESIDUAL_SOLVENTS_2_EQ: function (property, value) {
            return {data: {2: {eq: value}}};
        },
        RESIDUAL_SOLVENTS_2_RANK: function (property, value) {
            return {data: {2: {name: {rank: parseInt(value)}}}};
        },
        RESIDUAL_SOLVENTS_2_NAME: function (property, value) {
            return {data: {2: {name: {name: value}}}};
        },
        RESIDUAL_SOLVENTS_3_COMMENT: function (property, value) {
            return {data: {3: {comment: value}}};
        },
        RESIDUAL_SOLVENTS_3_EQ: function (property, value) {
            return {data: {3: {eq: value}}};
        },
        RESIDUAL_SOLVENTS_3_RANK: function (property, value) {
            return {data: {3: {name: {rank: parseInt(value)}}}};
        },
        RESIDUAL_SOLVENTS_3_NAME: function (property, value) {
            return {data: {3: {name: {name: value}}}};
        },
        RESIDUAL_SOLVENTS_4_COMMENT: function (property, value) {
            return {data: {4: {comment: value}}};
        },
        RESIDUAL_SOLVENTS_4_EQ: function (property, value) {
            return {data: {4: {eq: value}}};
        },
        RESIDUAL_SOLVENTS_4_RANK: function (property, value) {
            return {data: {4: {name: {rank: parseInt(value)}}}};
        },
        RESIDUAL_SOLVENTS_4_NAME: function (property, value) {
            return {data: {4: {name: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_STRING: function (property, value) {
            return {asString: value};
        },
        SOLUBILITY_SOLVENTS_0_COMMENT: function (property, value) {
            return {data: {0: {comment: value}}};
        },
        SOLUBILITY_SOLVENTS_0_TYPE: function (property, value) {
            return {data: {0: {type: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_0_RANK: function (property, value) {
            return {data: {0: {solventName: {rank: parseInt(value)}}}};
        },
        SOLUBILITY_SOLVENTS_0_NAME: function (property, value) {
            return {data: {0: {solventName: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_0_ENABLE: function (property, value) {
            return {data: {0: {solventName: {enable: value}}}};
        },
        SOLUBILITY_SOLVENTS_0_VALUE_NAME: function (property, value) {
            return {data: {0: {value: {value: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_0_VALUE: function (property, value) {
            return {data: {0: {value: {value: value}}}};
        },
        SOLUBILITY_SOLVENTS_0_VALUE_OPERATOR: function (property, value) {
            return {data: {0: {value: {operator: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_0_VALUE_UNIT: function (property, value) {
            return {data: {0: {value: {unit: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_1_COMMENT: function (property, value) {
            return {data: {1: {comment: value}}};
        },
        SOLUBILITY_SOLVENTS_1_TYPE: function (property, value) {
            return {data: {1: {type: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_1_RANK: function (property, value) {
            return {data: {1: {solventName: {rank: parseInt(value)}}}};
        },
        SOLUBILITY_SOLVENTS_1_NAME: function (property, value) {
            return {data: {1: {solventName: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_1_ENABLE: function (property, value) {
            return {data: {1: {solventName: {enable: value}}}};
        },
        SOLUBILITY_SOLVENTS_1_VALUE_NAME: function (property, value) {
            return {data: {1: {value: {value: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_1_VALUE: function (property, value) {
            return {data: {1: {value: {value: value}}}};
        },
        SOLUBILITY_SOLVENTS_1_VALUE_OPERATOR: function (property, value) {
            return {data: {1: {value: {operator: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_1_VALUE_UNIT: function (property, value) {
            return {data: {1: {value: {unit: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_2_COMMENT: function (property, value) {
            return {data: {2: {comment: value}}};
        },
        SOLUBILITY_SOLVENTS_2_TYPE: function (property, value) {
            return {data: {2: {type: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_2_RANK: function (property, value) {
            return {data: {2: {solventName: {rank: parseInt(value)}}}};
        },
        SOLUBILITY_SOLVENTS_2_NAME: function (property, value) {
            return {data: {2: {solventName: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_2_ENABLE: function (property, value) {
            return {data: {2: {solventName: {enable: value}}}};
        },
        SOLUBILITY_SOLVENTS_2_VALUE_NAME: function (property, value) {
            return {data: {2: {value: {value: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_2_VALUE: function (property, value) {
            return {data: {2: {value: {value: value}}}};
        },
        SOLUBILITY_SOLVENTS_2_VALUE_OPERATOR: function (property, value) {
            return {data: {2: {value: {operator: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_2_VALUE_UNIT: function (property, value) {
            return {data: {2: {value: {unit: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_3_COMMENT: function (property, value) {
            return {data: {3: {comment: value}}};
        },
        SOLUBILITY_SOLVENTS_3_TYPE: function (property, value) {
            return {data: {3: {type: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_3_RANK: function (property, value) {
            return {data: {3: {solventName: {rank: parseInt(value)}}}};
        },
        SOLUBILITY_SOLVENTS_3_NAME: function (property, value) {
            return {data: {3: {solventName: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_3_ENABLE: function (property, value) {
            return {data: {3: {solventName: {enable: value}}}};
        },
        SOLUBILITY_SOLVENTS_3_VALUE_NAME: function (property, value) {
            return {data: {3: {value: {value: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_3_VALUE: function (property, value) {
            return {data: {3: {value: {value: value}}}};
        },
        SOLUBILITY_SOLVENTS_3_VALUE_OPERATOR: function (property, value) {
            return {data: {3: {value: {operator: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_3_VALUE_UNIT: function (property, value) {
            return {data: {3: {value: {unit: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_4_COMMENT: function (property, value) {
            return {data: {4: {comment: value}}};
        },
        SOLUBILITY_SOLVENTS_4_TYPE: function (property, value) {
            return {data: {4: {type: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_4_RANK: function (property, value) {
            return {data: {4: {solventName: {rank: parseInt(value)}}}};
        },
        SOLUBILITY_SOLVENTS_4_NAME: function (property, value) {
            return {data: {4: {solventName: {name: value}}}};
        },
        SOLUBILITY_SOLVENTS_4_ENABLE: function (property, value) {
            return {data: {4: {solventName: {enable: value}}}};
        },
        SOLUBILITY_SOLVENTS_4_VALUE_NAME: function (property, value) {
            return {data: {4: {value: {value: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_4_VALUE: function (property, value) {
            return {data: {4: {value: {value: value}}}};
        },
        SOLUBILITY_SOLVENTS_4_VALUE_OPERATOR: function (property, value) {
            return {data: {4: {value: {operator: {name: value}}}}};
        },
        SOLUBILITY_SOLVENTS_4_VALUE_UNIT: function (property, value) {
            return {data: {4: {value: {unit: {name: value}}}}};
        },
        EXTERNAL_SUPPLIER_STRING: function (property, value) {
            return {asString: value};
        },
        EXTERNAL_SUPPLIER_REG_NUMBER: function (property, value) {
            return {catalogRegistryNumber: value};
        },
        EXTERNAL_SUPPLIER_CODE_AND_NAME: function (property, value) {
            return {codeAndName: {name: value}};
        },
        COMPOUND_PROTECTION: function (property, value) {
            return {name: value};
        },
        HEALTH_HAZARDS_STRING: function (property, value) {
            return {asString: value};
        },
        HEALTH_HAZARDS_0: function (property, value) {
            return {data: {0: value}};
        },
        HEALTH_HAZARDS_1: function (property, value) {
            return {data: {1: value}};
        },
        HEALTH_HAZARDS_2: function (property, value) {
            return {data: {2: value}};
        },
        HEALTH_HAZARDS_3: function (property, value) {
            return {data: {3: value}};
        },
        HEALTH_HAZARDS_4: function (property, value) {
            return {data: {4: value}};
        },
        HEALTH_HAZARDS_5: function (property, value) {
            return {data: {5: value}};
        },
        HANDLING_PRECAUTIONS_STRING: function (property, value) {
            return {asString: value};
        },
        HANDLING_PRECAUTIONS_0: function (property, value) {
            return {data: {0: value}};
        },
        HANDLING_PRECAUTIONS_1: function (property, value) {
            return {data: {1: value}};
        },
        HANDLING_PRECAUTIONS_2: function (property, value) {
            return {data: {2: value}};
        },
        HANDLING_PRECAUTIONS_3: function (property, value) {
            return {data: {3: value}};
        },
        HANDLING_PRECAUTIONS_4: function (property, value) {
            return {data: {4: value}};
        },
        STORAGE_INSTRUCTIONS_STRING: function (property, value) {
            return {asString: value};
        },
        STORAGE_INSTRUCTIONS_0: function (property, value) {
            return {data: {0: value}};
        },
        STORAGE_INSTRUCTIONS_1: function (property, value) {
            return {data: {1: value}};
        },
        STORAGE_INSTRUCTIONS_2: function (property, value) {
            return {data: {2: value}};
        },
        STORAGE_INSTRUCTIONS_3: function (property, value) {
            return {data: {3: value}};
        },
        BATCH_COMMENTS: function (property, value) {
            return value;
        }
    };

    return {
        additionalFormatFunctions: additionalFormatFunctions
    };

    function getItem(list, prop, value) {
        return _.find(list, function (item) {
            return item[prop].toUpperCase() === value.toUpperCase();
        });
    }

}