angular
    .module('indigoeln')
    .constant('SdConstants', {
        sdProperties: [
            {
                code: 'REGISTRATION_STATUS',
                name: 'registrationStatus',
                propName: 'registrationStatus'
            },
            {
                code: 'CONVERSATIONAL_BATCH_NUMBER',
                name: 'conversationalBatchNumber',
                propName: 'conversationalBatchNumber'
            },
            {
                code: 'VIRTUAL_COMPOUND_ID',
                name: 'virtualCompoundId',
                propName: 'virtualCompoundId'
            },
            {
                code: 'COMPOUND_SOURCE_CODE',
                name: 'source',
                path: ['source', 'name'],
                propName: 'Source',
                subPropName: 'name'
            },
            {
                code: 'COMPOUND_SOURCE_DETAIL_CODE',
                name: 'sourceDetail',
                path: ['sourceDetail', 'name'],
                propName: 'Source Details',
                subPropName: 'name'
            },
            {
                code: 'STEREOISOMER_CODE',
                name: 'stereoisomer',
                path: ['stereoisomer', 'name'],
                propName: 'Stereoisomer Code',
                subPropName: 'name'
            },
            {
                code: 'GLOBAL_SALT_CODE',
                name: 'saltCode',
                path: ['saltCode', 'regValue'],
                propName: 'saltCode',
                subPropName: 'regValue'
            },
            {
                code: 'GLOBAL_SALT_EQ',
                name: 'saltEq',
                path: ['saltEq', 'value']
            },
            {
                code: 'STRUCTURE_COMMENT',
                name: 'structureComments',
                propName: 'structureComments'
            },
            {
                code: 'COMPOUND_STATE',
                name: 'compoundState',
                path: ['compoundState', 'name'],
                propName: 'Compound State',
                subPropName: 'name'
            },
            {
                code: 'PRECURSORS',
                name: 'precursors',
                propName: 'precursors'
            },
            {
                name: 'purity',
                code: 'PURITY_STRING',
                subPropName: 'asString'
            },
            {
                code: 'PURITY_COMENTS',
                name: 'purity',
                childrenLength: 5,
                path: ['purity', 'data', '0', 'comments']
            },
            {
                code: 'PURITY_DETERMINED',
                name: 'purity',
                childrenLength: 5,
                path: ['purity', 'data', '0', 'determinedBy']
            },
            {
                code: 'PURITY_OPERATOR_NAME',
                name: 'purity',
                childrenLength: 5,
                path: ['purity', 'data', '0', 'operator', 'name']
            },
            {
                code: 'PURITY_VALUE',
                name: 'purity',
                childrenLength: 5,
                path: ['purity', 'data', '0', 'value']
            },
            {
                code: 'MELTING_POINT',
                name: 'meltingPoint',
                path: ['meltingPoint', 'asString']
            },
            {
                code: 'MELTING_POINT_COMMENTS',
                name: 'meltingPoint',
                path: ['meltingPoint', 'comments']
            },
            {
                code: 'MELTING_POINT_LOWER',
                name: 'meltingPoint',
                path: ['meltingPoint', 'lower']
            },
            {
                code: 'MELTING_POINT_UPPER',
                name: 'meltingPoint',
                path: ['meltingPoint', 'upper']
            },
            {
                code: 'RESIDUAL_SOLVENTS_STRING',
                name: 'residualSolvents',
                path: ['residualSolvents', 'asString']
            },
            {
                code: 'RESIDUAL_SOLVENTS_COMMENT',
                name: 'residualSolvents',
                childrenLength: 5,
                path: ['residualSolvents', 'data', '0', 'comment']
            },
            {
                code: 'RESIDUAL_SOLVENTS_EQ',
                name: 'residualSolvents',
                childrenLength: 5,
                path: ['residualSolvents', 'data', '0', 'eq']
            },
            {
                code: 'RESIDUAL_SOLVENTS_RANK',
                name: 'residualSolvents',
                childrenLength: 5,
                path: ['residualSolvents', 'data', '0', 'name', 'rank']
            },
            {
                code: 'RESIDUAL_SOLVENTS_NAME',
                name: 'residualSolvents',
                childrenLength: 5,
                path: ['residualSolvents', 'data', '0', 'name', 'name']
            },
            {
                code: 'SOLUBILITY_SOLVENTS_STRING',
                name: 'solubility',
                path: ['solubility', 'asString']
            },
            {
                code: 'SOLUBILITY_SOLVENTS_COMMENT',
                name: 'solubility',
                childrenLength: 5,
                path: ['solubility', 'data', '0', 'comment']
            },
            {
                code: 'SOLUBILITY_SOLVENTS_TYPE',
                name: 'solubility',
                childrenLength: 5,
                path: ['solubility', 'data', '0', 'type', 'name']
            },
            {
                code: 'SOLUBILITY_SOLVENTS_RANK',
                name: 'solubility',
                childrenLength: 5,
                path: ['solubility', 'data', '0', 'solventName', 'rank']
            },
            {
                code: 'SOLUBILITY_SOLVENTS_NAME',
                name: 'solubility',
                childrenLength: 5,
                path: ['solubility', 'data', '0', 'solventName', 'name']
            },
            {
                code: 'SOLUBILITY_SOLVENTS_ENABLE',
                name: 'solubility',
                childrenLength: 5,
                path: ['solubility', 'data', '0', 'solventName', 'enable']
            },
            {
                code: 'SOLUBILITY_SOLVENTS_VALUE_NAME',
                name: 'solubility',
                childrenLength: 5,
                path: ['solubility', 'data', '0', 'value', 'value', 'name']
            },
            {
                code: 'SOLUBILITY_SOLVENTS_VALUE',
                name: 'solubility',
                childrenLength: 5,
                path: ['solubility', 'data', '0', 'value', 'value']
            },
            {
                code: 'SOLUBILITY_SOLVENTS_VALUE_OPERATOR',
                name: 'solubility',
                childrenLength: 5,
                path: ['solubility', 'data', '0', 'value', 'operator', 'name']
            },
            {
                code: 'SOLUBILITY_SOLVENTS_VALUE_UNIT',
                name: 'solubility',
                childrenLength: 5,
                path: ['solubility', 'data', '0', 'value', 'unit', 'name']
            },
            {
                code: 'EXTERNAL_SUPPLIER_STRING',
                name: 'externalSupplier',
                path: ['externalSupplier', 'asString']
            },
            {
                code: 'EXTERNAL_SUPPLIER_REG_NUMBER',
                name: 'externalSupplier',
                path: ['externalSupplier', 'catalogRegistryNumber']
            },
            {
                code: 'EXTERNAL_SUPPLIER_CODE_AND_NAME',
                name: 'externalSupplier',
                path: ['externalSupplier', 'codeAndName', 'name']
            },
            {
                code: 'COMPOUND_PROTECTION',
                name: 'compoundProtection',
                path: ['compoundProtection', 'name']
            },
            {
                code: 'HEALTH_HAZARDS_STRING',
                name: 'healthHazards',
                path: ['healthHazards', 'asString']
            },
            {
                code: 'HEALTH_HAZARDS',
                name: 'healthHazards',
                childrenLength: 6,
                path: ['healthHazards', 'data']
            },
            {
                code: 'HANDLING_PRECAUTIONS_STRING',
                name: 'handlingPrecautions',
                path: ['handlingPrecautions', 'asString']
            },
            {
                code: 'HANDLING_PRECAUTIONS',
                name: 'handlingPrecautions',
                childrenLength: 5,
                path: ['handlingPrecautions', 'data']
            },
            {
                code: 'STORAGE_INSTRUCTIONS_STRING',
                name: 'storageInstructions',
                path: ['storageInstructions', 'asString']
            },
            {
                code: 'STORAGE_INSTRUCTIONS',
                name: 'storageInstructions',
                childrenLength: 4,
                path: ['storageInstructions', 'data']
            },
            {
                code: 'BATCH_COMMENTS',
                name: 'comments',
                path: ['comments']
            }
        ]
    });
