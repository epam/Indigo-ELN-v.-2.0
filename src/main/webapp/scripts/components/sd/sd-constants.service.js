angular
    .module('indigoeln')
    .constant('sdConstants', [
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
            path: 'name',
            propName: 'Source',
            subPropName: 'name'
        },
        {
            code: 'COMPOUND_SOURCE_DETAIL_CODE',
            name: 'sourceDetail',
            path: 'name',
            propName: 'Source Details',
            subPropName: 'name'
        },
        {
            code: 'STEREOISOMER_CODE',
            name: 'stereoisomer',
            path: 'name',
            propName: 'Stereoisomer Code',
            subPropName: 'name'
        },
        {
            code: 'GLOBAL_SALT_CODE',
            name: 'saltCode',
            path: 'regValue',
            propName: 'saltCode',
            subPropName: 'regValue'
        },
        {
            code: 'GLOBAL_SALT_EQ',
            name: 'saltEq',
            path: 'value'
        },
        {
            code: 'STRUCTURE_COMMENT',
            name: 'structureComments',
            propName: 'structureComments'
        },
        {
            code: 'COMPOUND_STATE',
            name: 'compoundState',
            path: 'name',
            propName: 'Compound State',
            subPropName: 'name'
        },
        {
            code: 'PRECURSORS',
            name: 'precursors',
            propName: 'precursors'
        },
        {
            code: 'PURITY_STRING',
            name: 'purity',
            path: 'asString',
            subPropName: 'asString'
        },
        {
            code: 'PURITY_COMENTS',
            name: 'purity',
            childrenLength: 5,
            path: 'data.<%= index =>.comments'
        },
        {
            code: 'PURITY_DETERMINED',
            name: 'purity',
            childrenLength: 5,
            path: 'data.<%= index =>.determinedBy'
        },
        {
            code: 'PURITY_OPERATOR_NAME',
            name: 'purity',
            childrenLength: 5,
            path: 'data.<%= index =>.operator.name'
        },
        {
            code: 'PURITY_VALUE',
            name: 'purity',
            childrenLength: 5,
            path: 'data.<%= index =>.value',
            isNumeric: true
        },
        {
            code: 'MELTING_POINT',
            name: 'meltingPoint',
            path: 'asString'
        },
        {
            code: 'MELTING_POINT_COMMENTS',
            name: 'meltingPoint',
            path: 'comments'
        },
        {
            code: 'MELTING_POINT_LOWER',
            name: 'meltingPoint',
            path: 'lower',
            isNumeric: true
        },
        {
            code: 'MELTING_POINT_UPPER',
            name: 'meltingPoint',
            path: 'upper',
            isNumeric: true
        },
        {
            code: 'RESIDUAL_SOLVENTS_STRING',
            name: 'residualSolvents',
            path: 'asString'
        },
        {
            code: 'RESIDUAL_SOLVENTS_COMMENT',
            name: 'residualSolvents',
            childrenLength: 5,
            path: 'data.<%= index =>.comment'
        },
        {
            code: 'RESIDUAL_SOLVENTS_EQ',
            name: 'residualSolvents',
            childrenLength: 5,
            path: 'data.<%= index =>.eq'
        },
        {
            code: 'RESIDUAL_SOLVENTS_RANK',
            name: 'residualSolvents',
            childrenLength: 5,
            path: 'data.<%= index =>.name.rank',
            isNumeric: true
        },
        {
            code: 'RESIDUAL_SOLVENTS_NAME',
            name: 'residualSolvents',
            childrenLength: 5,
            path: 'data.<%= index =>.name.name'
        },
        {
            code: 'SOLUBILITY_SOLVENTS_STRING',
            name: 'solubility',
            path: 'asString'
        },
        {
            code: 'SOLUBILITY_SOLVENTS_COMMENT',
            name: 'solubility',
            childrenLength: 5,
            path: 'data.<%= index =>.comment'
        },
        {
            code: 'SOLUBILITY_SOLVENTS_TYPE',
            name: 'solubility',
            childrenLength: 5,
            path: 'data.<%= index =>.type.name'
        },
        {
            code: 'SOLUBILITY_SOLVENTS_RANK',
            name: 'solubility',
            childrenLength: 5,
            path: 'data.<%= index =>.solventName.rank',
            isNumeric: true
        },
        {
            code: 'SOLUBILITY_SOLVENTS_NAME',
            name: 'solubility',
            childrenLength: 5,
            path: 'data.<%= index =>.solventName.name'
        },
        {
            code: 'SOLUBILITY_SOLVENTS_ENABLE',
            name: 'solubility',
            childrenLength: 5,
            path: 'data.<%= index =>.solventName.enable'
        },
        {
            code: 'SOLUBILITY_SOLVENTS_VALUE_NAME',
            name: 'solubility',
            childrenLength: 5,
            path: 'data.<%= index =>.value.value.name'
        },
        {
            code: 'SOLUBILITY_SOLVENTS_VALUE',
            name: 'solubility',
            childrenLength: 5,
            path: 'data.<%= index =>.value.value'
        },
        {
            code: 'SOLUBILITY_SOLVENTS_VALUE_OPERATOR',
            name: 'solubility',
            childrenLength: 5,
            path: 'data.<%= index =>.value.operator.name'
        },
        {
            code: 'SOLUBILITY_SOLVENTS_VALUE_UNIT',
            name: 'solubility',
            childrenLength: 5,
            path: 'data.<%= index =>.value.unit.name'
        },
        {
            code: 'EXTERNAL_SUPPLIER_STRING',
            name: 'externalSupplier',
            path: 'asString'
        },
        {
            code: 'EXTERNAL_SUPPLIER_REG_NUMBER',
            name: 'externalSupplier',
            path: 'catalogRegistryNumber'
        },
        {
            code: 'EXTERNAL_SUPPLIER_CODE_AND_NAME',
            name: 'externalSupplier',
            path: 'codeAndName.name'
        },
        {
            code: 'COMPOUND_PROTECTION',
            name: 'compoundProtection',
            path: 'name'
        },
        {
            code: 'HEALTH_HAZARDS_STRING',
            name: 'healthHazards',
            path: 'asString'
        },
        {
            code: 'HEALTH_HAZARDS',
            name: 'healthHazards',
            childrenLength: 6,
            path: 'data.<%= index =>'
        },
        {
            code: 'HANDLING_PRECAUTIONS_STRING',
            name: 'handlingPrecautions',
            path: 'asString'
        },
        {
            code: 'HANDLING_PRECAUTIONS',
            name: 'handlingPrecautions',
            childrenLength: 5,
            path: 'data.<%= index =>'
        },
        {
            code: 'STORAGE_INSTRUCTIONS_STRING',
            name: 'storageInstructions',
            path: 'asString'
        },
        {
            code: 'STORAGE_INSTRUCTIONS',
            name: 'storageInstructions',
            childrenLength: 4,
            path: 'data.<%= index =>'
        },
        {
            code: 'BATCH_COMMENTS',
            name: 'comments'
        },
        {
            code: 'BUTCH_TYPE',
            name: 'butchType'
        },
        {
            code: 'THEO_MOLES_DISPLAY_VALUE',
            name: 'theoMoles',
            path: 'displayValue'
        },
        {
            code: 'THEO_MOLES_VALUE',
            name: 'theoMoles',
            path: 'value',
            isNumeric: true
        },
        {
            code: 'THEO_MOLES_UNIT',
            name: 'theoMoles',
            path: 'unit'
        },
        {
            code: 'THEO_WEIGHT_DISPLAY_VALUE',
            name: 'theoWeight',
            path: 'displayValue'
        },
        {
            code: 'THEO_WEIGHT_VALUE',
            name: 'theoWeight',
            path: 'value',
            isNumeric: true
        },
        {
            code: 'THEO_WEIGHT_UNIT',
            name: 'theoWeight',
            path: 'unit'
        },
        {
            code: 'TOTAL_MOLES_DISPLAY_VALUE',
            name: 'totalMoles',
            path: 'displayValue'
        },
        {
            code: 'TOTAL_MOLES_VALUE',
            name: 'totalMoles',
            path: 'value',
            isNumeric: true
        },
        {
            code: 'TOTAL_MOLES_UNIT',
            name: 'totalMoles',
            path: 'unit'
        },
        {
            code: 'TOTAL_VOLUME_DISPLAY_VALUE',
            name: 'totalVolume',
            path: 'displayValue'
        },
        {
            code: 'TOTAL_VOLUME_VALUE',
            name: 'totalVolume',
            path: 'value',
            isNumeric: true
        },
        {
            code: 'TOTAL_VOLUME_UNIT',
            name: 'totalVolume',
            path: 'unit'
        },
        {
            code: 'TOTAL_WEIGHT_DISPLAY_VALUE',
            name: 'totalWeight',
            path: 'displayValue'
        },
        {
            code: 'TOTAL_WEIGHT_VALUE',
            name: 'totalWeight',
            path: 'value',
            isNumeric: true
        },
        {
            code: 'TOTAL_WEIGHT_UNIT',
            name: 'totalWeight',
            path: 'unit'
        }
    ]
    );
