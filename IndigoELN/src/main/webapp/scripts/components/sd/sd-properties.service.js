angular
    .module('indigoeln')
    .factory('sdProperties', sdProperties);

/* @ngInject */
function sdProperties() {
    var constants = [
        {
            export: {
                name: 'REGISTRATION_STATUS',
                prop: ['registrationStatus']
            }
        },
        {
            export: {
                name: 'CONVERSATIONAL_BATCH_NUMBER',
                prop: ['conversationalBatchNumber']
            }
        },
        {
            export: {
                name: 'VIRTUAL_COMPOUND_ID',
                prop: ['virtualCompoundId']
            }
        },
        {
            export: {
                name: 'COMPOUND_SOURCE_CODE',
                prop: ['source', 'name'],
                propName: 'Source',
                subPropName: 'name'
            }
        },
        {
            export: {
                name: 'COMPOUND_SOURCE_DETAIL_CODE',
                prop: ['sourceDetail', 'name']
            }
        },
        {
            export: {
                name: 'STEREOISOMER_CODE',
                prop: ['stereoisomer', 'name']
            }
        },
        {
            export: {
                name: 'GLOBAL_SALT_CODE',
                prop: ['saltCode', 'regValue']
            }
        },
        {
            export: {
                name: 'GLOBAL_SALT_EQ',
                prop: ['saltEq', 'value']
            }
        },
        {
            export: {
                name: 'STRUCTURE_COMMENT',
                prop: ['structureComments']
            }
        },
        {
            export: {
                name: 'COMPOUND_STATE',
                prop: ['compoundState', 'name']
            }
        },
        {
            export: {
                name: 'PRECURSORS',
                prop: ['precursors']
            }
        },
        {
            export: {
                name: 'PURITY_STRING',
                prop: ['purity', 'asString']
            }
        },
        {
            export: {
                name: 'PURITY_COMENTS_0',
                prop: ['purity', 'data', '0', 'comments']
            }
        },
        {
            export: {
                name: 'PURITY_COMENTS_1',
                prop: ['purity', 'data', '1', 'comments']
            }
        },
        {
            export: {
                name: 'PURITY_COMENTS_2',
                prop: ['purity', 'data', '2', 'comments']
            }
        },
        {
            export: {
                name: 'PURITY_COMENTS_3',
                prop: ['purity', 'data', '3', 'comments']
            }
        },
        {
            export: {
                name: 'PURITY_COMENTS_4',
                prop: ['purity', 'data', '4', 'comments']
            }
        },
        {
            export: {
                name: 'PURITY_DETERMINED_0',
                prop: ['purity', 'data', '0', 'determinedBy']
            }
        },
        {
            export: {
                name: 'PURITY_DETERMINED_1',
                prop: ['purity', 'data', '1', 'determinedBy']
            }
        },
        {
            export: {
                name: 'PURITY_DETERMINED_2',
                prop: ['purity', 'data', '2', 'determinedBy']
            }
        },
        {
            export: {
                name: 'PURITY_DETERMINED_3',
                prop: ['purity', 'data', '3', 'determinedBy']
            }
        },
        {
            export: {
                name: 'PURITY_DETERMINED_4',
                prop: ['purity', 'data', '4', 'determinedBy']
            }
        },
        {
            export: {
                name: 'PURITY_OPERATOR_NAME_0',
                prop: ['purity', 'data', '0', 'operator', 'name']
            }
        },
        {
            export: {
                name: 'PURITY_OPERATOR_NAME_1',
                prop: ['purity', 'data', '1', 'operator', 'name']
            }
        },
        {
            export: {
                name: 'PURITY_OPERATOR_NAME_2',
                prop: ['purity', 'data', '2', 'operator', 'name']
            }
        },
        {
            export: {
                name: 'PURITY_OPERATOR_NAME_3',
                prop: ['purity', 'data', '3', 'operator', 'name']
            }
        },
        {
            export: {
                name: 'PURITY_OPERATOR_NAME_4',
                prop: ['purity', 'data', '4', 'operator', 'name']
            }
        },
        {
            export: {
                name: 'PURITY_VALUE_0',
                prop: ['purity', 'data', '0', 'value']
            }
        },
        {
            export: {
                name: 'PURITY_VALUE_1',
                prop: ['purity', 'data', '1', 'value']
            }
        },
        {
            export: {
                name: 'PURITY_VALUE_2',
                prop: ['purity', 'data', '2', 'value']
            }
        },
        {
            export: {
                name: 'PURITY_VALUE_3',
                prop: ['purity', 'data', '3', 'value']
            }
        },
        {
            export: {
                name: 'PURITY_VALUE_4',
                prop: ['purity', 'data', '4', 'value']
            }
        },
        {
            export: {
                name: 'MELTING_POINT',
                prop: ['meltingPoint', 'asString']
            }
        },
        {
            export: {
                name: 'MELTING_POINT_COMMENTS',
                prop: ['meltingPoint', 'comments']
            }
        },
        {
            export: {
                name: 'MELTING_POINT_LOWER',
                prop: ['meltingPoint', 'lower']
            }
        },
        {
            export: {
                name: 'MELTING_POINT_UPPER',
                prop: ['meltingPoint', 'upper']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_STRING',
                prop: ['residualSolvents', 'asString']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_COMMENT_0',
                prop: ['residualSolvents', 'data', '0', 'comment']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_COMMENT_1',
                prop: ['residualSolvents', 'data', '1', 'comment']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_COMMENT_2',
                prop: ['residualSolvents', 'data', '2', 'comment']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_COMMENT_3',
                prop: ['residualSolvents', 'data', '3', 'comment']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_COMMENT_4',
                prop: ['residualSolvents', 'data', '4', 'comment']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_EQ_0',
                prop: ['residualSolvents', 'data', '0', 'eq']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_EQ_1',
                prop: ['residualSolvents', 'data', '1', 'eq']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_EQ_2',
                prop: ['residualSolvents', 'data', '2', 'eq']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_EQ_3',
                prop: ['residualSolvents', 'data', '3', 'eq']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_EQ_4',
                prop: ['residualSolvents', 'data', '4', 'eq']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_RANK_0',
                prop: ['residualSolvents', 'data', '0', 'name', 'rank']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_RANK_1',
                prop: ['residualSolvents', 'data', '0', 'name', 'rank']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_RANK_2',
                prop: ['residualSolvents', 'data', '0', 'name', 'rank']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_RANK_3',
                prop: ['residualSolvents', 'data', '0', 'name', 'rank']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_RANK_4',
                prop: ['residualSolvents', 'data', '0', 'name', 'rank']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_NAME_0',
                prop: ['residualSolvents', 'data', '0', 'name', 'name']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_NAME_1',
                prop: ['residualSolvents', 'data', '0', 'name', 'name']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_NAME_2',
                prop: ['residualSolvents', 'data', '0', 'name', 'name']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_NAME_3',
                prop: ['residualSolvents', 'data', '0', 'name', 'name']
            }
        },
        {
            export: {
                name: 'RESIDUAL_SOLVENTS_NAME_4',
                prop: ['residualSolvents', 'data', '0', 'name', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_STRING',
                prop: ['solubility', 'asString']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_COMMENT_0',
                prop: ['solubility', 'data', '0', 'comment']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_COMMENT_1',
                prop: ['solubility', 'data', '1', 'comment']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_COMMENT_2',
                prop: ['solubility', 'data', '2', 'comment']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_COMMENT_3',
                prop: ['solubility', 'data', '3', 'comment']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_COMMENT_4',
                prop: ['solubility', 'data', '4', 'comment']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_TYPE_0',
                prop: ['solubility', 'data', '0', 'type', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_TYPE_1',
                prop: ['solubility', 'data', '1', 'type', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_TYPE_2',
                prop: ['solubility', 'data', '2', 'type', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_TYPE_3',
                prop: ['solubility', 'data', '3', 'type', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_TYPE_4',
                prop: ['solubility', 'data', '4', 'type', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_RANK_0',
                prop: ['solubility', 'data', '0', 'solventName', 'rank']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_RANK_1',
                prop: ['solubility', 'data', '1', 'solventName', 'rank']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_RANK_2',
                prop: ['solubility', 'data', '2', 'solventName', 'rank']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_RANK_3',
                prop: ['solubility', 'data', '3', 'solventName', 'rank']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_RANK_4',
                prop: ['solubility', 'data', '4', 'solventName', 'rank']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_NAME_0',
                prop: ['solubility', 'data', '0', 'solventName', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_NAME_1',
                prop: ['solubility', 'data', '1', 'solventName', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_NAME_2',
                prop: ['solubility', 'data', '2', 'solventName', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_NAME_3',
                prop: ['solubility', 'data', '3', 'solventName', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_NAME_4',
                prop: ['solubility', 'data', '4', 'solventName', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_ENABLE_0',
                prop: ['solubility', 'data', '0', 'solventName', 'enable']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_ENABLE_1',
                prop: ['solubility', 'data', '1', 'solventName', 'enable']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_ENABLE_2',
                prop: ['solubility', 'data', '2', 'solventName', 'enable']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_ENABLE_3',
                prop: ['solubility', 'data', '3', 'solventName', 'enable']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_ENABLE_4',
                prop: ['solubility', 'data', '4', 'solventName', 'enable']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_NAME_0',
                prop: ['solubility', 'data', '0', 'value', 'value', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_NAME_1',
                prop: ['solubility', 'data', '1', 'value', 'value', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_NAME_2',
                prop: ['solubility', 'data', '2', 'value', 'value', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_NAME_3',
                prop: ['solubility', 'data', '3', 'value', 'value', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_NAME_4',
                prop: ['solubility', 'data', '4', 'value', 'value', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_0',
                prop: ['solubility', 'data', '0', 'value', 'value']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_1',
                prop: ['solubility', 'data', '1', 'value', 'value']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_2',
                prop: ['solubility', 'data', '2', 'value', 'value']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_3',
                prop: ['solubility', 'data', '3', 'value', 'value']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_4',
                prop: ['solubility', 'data', '4', 'value', 'value']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_OPERATOR_0',
                prop: ['solubility', 'data', '0', 'value', 'operator', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_OPERATOR_1',
                prop: ['solubility', 'data', '1', 'value', 'operator', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_OPERATOR_2',
                prop: ['solubility', 'data', '2', 'value', 'operator', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_OPERATOR_3',
                prop: ['solubility', 'data', '3', 'value', 'operator', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_OPERATOR_4',
                prop: ['solubility', 'data', '4', 'value', 'operator', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_UNIT_0',
                prop: ['solubility', 'data', '0', 'value', 'unit', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_UNIT_1',
                prop: ['solubility', 'data', '1', 'value', 'unit', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_UNIT_2',
                prop: ['solubility', 'data', '2', 'value', 'unit', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_UNIT_3',
                prop: ['solubility', 'data', '3', 'value', 'unit', 'name']
            }
        },
        {
            export: {
                name: 'SOLUBILITY_SOLVENTS_VALUE_UNIT_4',
                prop: ['solubility', 'data', '4', 'value', 'unit', 'name']
            }
        },
        {
            export: {
                name: 'EXTERNAL_SUPPLIER_STRING',
                prop: ['externalSupplier', 'asString']
            }
        },
        {
            export: {
                name: 'EXTERNAL_SUPPLIER_REG_NUMBER',
                prop: ['externalSupplier', 'catalogRegistryNumber']
            }
        },
        {
            export: {
                name: 'EXTERNAL_SUPPLIER_CODE_AND_NAME',
                prop: ['externalSupplier', 'codeAndName', 'name']
            }
        },
        {
            export: {
                name: 'COMPOUND_PROTECTION',
                prop: ['compoundProtection', 'name']
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_STRING',
                prop: ['healthHazards', 'asString']
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_0',
                prop: ['healthHazards', 'data', '0']
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_1',
                prop: ['healthHazards', 'data', '1']
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_2',
                prop: ['healthHazards', 'data', '2']
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_3',
                prop: ['healthHazards', 'data', '3']
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_4',
                prop: ['healthHazards', 'data', '4']
            }
        },
        {
            export: {
                name: 'HEALTH_HAZARDS_5',
                prop: ['healthHazards', 'data', '5']
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_STRING',
                prop: ['handlingPrecautions', 'asString']
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_0',
                prop: ['handlingPrecautions', 'data', '0']
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_1',
                prop: ['handlingPrecautions', 'data', '1']
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_2',
                prop: ['handlingPrecautions', 'data', '2']
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_3',
                prop: ['handlingPrecautions', 'data', '3']
            }
        },
        {
            export: {
                name: 'HANDLING_PRECAUTIONS_4',
                prop: ['handlingPrecautions', 'data', '4']
            }
        },
        {
            export: {
                name: 'STORAGE_INSTRUCTIONS_STRING',
                prop: ['storageInstructions', 'asString']
            }
        },
        {
            export: {
                name: 'STORAGE_INSTRUCTIONS_0',
                prop: ['storageInstructions', 'data', '0']
            }
        },
        {
            export: {
                name: 'STORAGE_INSTRUCTIONS_1',
                prop: ['storageInstructions', 'data', '1']
            }
        },
        {
            export: {
                name: 'STORAGE_INSTRUCTIONS_2',
                prop: ['storageInstructions', 'data', '2']
            }
        },
        {
            export: {
                name: 'STORAGE_INSTRUCTIONS_3',
                prop: ['storageInstructions', 'data', '3']
            }
        },
        {
            export: {
                name: 'BATCH_COMMENTS',
                prop: ['comments']
            }
        }
    ];

    return {
        constants: constants
    };
}
