angular
    .module('indigoeln')
    .constant('SdConstants',
        {
            1: {
                code: 'REGISTRATION_STATUS',
                name: 'registrationStatus',
                propName: 'registrationStatus'
            },
            2: {
                code: 'CONVERSATIONAL_BATCH_NUMBER',
                name: 'conversationalBatchNumber',
                propName: 'conversationalBatchNumber'
            },
            3: {
                code: 'VIRTUAL_COMPOUND_ID',
                name: 'virtualCompoundId',
                propName: 'virtualCompoundId'
            },
            4: {
                code: 'COMPOUND_SOURCE_CODE',
                name: 'source',
                path: ['source', 'name'],
                propName: 'Source',
                subPropName: 'name'
            },
            5: {
                code: 'COMPOUND_SOURCE_DETAIL_CODE',
                name: 'sourceDetail',
                path: ['sourceDetail', 'name'],
                propName: 'Source Details',
                subPropName: 'name'
            },
            6: {
                code: 'STEREOISOMER_CODE',
                name: 'stereoisomer',
                path: ['stereoisomer', 'name'],
                propName: 'Stereoisomer Code',
                subPropName: 'name'
            },
            7: {
                code: 'GLOBAL_SALT_CODE',
                name: 'saltCode',
                path: ['saltCode', 'regValue'],
                propName: 'saltCode',
                subPropName: 'regValue'
            },
            8: {
                code: 'GLOBAL_SALT_EQ',
                name: 'saltEq',
                path: ['saltEq', 'value']
            },
            9: {
                code: 'STRUCTURE_COMMENT',
                name: 'structureComments',
                propName: 'structureComments'
            },
            10: {
                code: 'COMPOUND_STATE',
                name: 'compoundState',
                path: ['compoundState', 'name'],
                propName: 'Compound State',
                subPropName: 'name'
            },
            11: {
                code: 'PRECURSORS',
                name: 'precursors',
                propName: 'precursors'
            },
            12: {
                name: 'purity',
                code: 'PURITY_STRING',
                subPropName: 'asString'
            },
            13: {
                code: 'PURITY_0_COMENTS',
                name: 'purity',
                path: ['purity', 'data', '0', 'comments']
            },
            14: {
                code: 'PURITY_0_DETERMINED',
                name: 'purity',
                path: ['purity', 'data', '0', 'determinedBy']
            },
            15: {
                code: 'PURITY_0_OPERATOR_NAME',
                name: 'purity',
                path: ['purity', 'data', '0', 'operator', 'name']
            },
            16: {
                code: 'PURITY_0_VALUE',
                name: 'purity',
                path: ['purity', 'data', '0', 'value']
            },
            17: {
                code: 'PURITY_1_COMENTS',
                name: 'purity',
                path: ['purity', 'data', '1', 'comments']
            },
            18: {
                code: 'PURITY_1_DETERMINED',
                name: 'purity',
                path: ['purity', 'data', '1', 'determinedBy']
            },
            19: {
                code: 'PURITY_1_OPERATOR_NAME',
                name: 'purity',
                path: ['purity', 'data', '1', 'operator', 'name']
            },
            20: {
                code: 'PURITY_1_VALUE',
                name: 'purity',
                path: ['purity', 'data', '1', 'value']
            },
            21: {
                code: 'PURITY_2_COMENTS',
                name: 'purity',
                path: ['purity', 'data', '2', 'comments']
            },
            22: {
                code: 'PURITY_2_DETERMINED',
                name: 'purity',
                path: ['purity', 'data', '2', 'determinedBy']
            },
            23: {
                code: 'PURITY_2_OPERATOR_NAME',
                name: 'purity',
                path: ['purity', 'data', '2', 'operator', 'name']
            },
            24: {
                code: 'PURITY_2_VALUE',
                name: 'purity',
                path: ['purity', 'data', '2', 'value']
            },
            25: {
                code: 'PURITY_3_COMENTS',
                name: 'purity',
                path: ['purity', 'data', '3', 'comments']
            },
            26: {
                code: 'PURITY_3_DETERMINED',
                name: 'purity',
                path: ['purity', 'data', '3', 'determinedBy']
            },
            27: {
                code: 'PURITY_3_OPERATOR_NAME',
                name: 'purity',
                path: ['purity', 'data', '3', 'operator', 'name']
            },
            28: {
                code: 'PURITY_3_VALUE',
                name: 'purity',
                path: ['purity', 'data', '3', 'value']
            },
            29: {
                code: 'PURITY_4_COMENTS',
                name: 'purity',
                path: ['purity', 'data', '4', 'comments']
            },
            30: {
                code: 'PURITY_4_DETERMINED',
                name: 'purity',
                path: ['purity', 'data', '4', 'determinedBy']
            },
            31: {
                code: 'PURITY_4_OPERATOR_NAME',
                name: 'purity',
                path: ['purity', 'data', '4', 'operator', 'name']
            },
            32: {
                code: 'PURITY_4_VALUE',
                name: 'purity',
                path: ['purity', 'data', '4', 'value']
            },
            33: {
                code: 'MELTING_POINT',
                name: 'meltingPoint',
                path: ['meltingPoint', 'asString']
            },
            34: {
                code: 'MELTING_POINT_COMMENTS',
                name: 'meltingPoint',
                path: ['meltingPoint', 'comments']
            },
            35: {
                code: 'MELTING_POINT_LOWER',
                name: 'meltingPoint',
                path: ['meltingPoint', 'lower']
            },
            36: {
                code: 'MELTING_POINT_UPPER',
                name: 'meltingPoint',
                path: ['meltingPoint', 'upper']
            },
            37: {
                code: 'RESIDUAL_SOLVENTS_STRING',
                name: 'residualSolvents',
                path: ['residualSolvents', 'asString']
            },
            38: {
                code: 'RESIDUAL_SOLVENTS_0_COMMENT',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '0', 'comment']
            },
            39: {
                code: 'RESIDUAL_SOLVENTS_0_EQ',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '0', 'eq']
            },
            40: {
                code: 'RESIDUAL_SOLVENTS_0_RANK',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '0', 'name', 'rank']
            },
            41: {
                code: 'RESIDUAL_SOLVENTS_0_NAME',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '0', 'name', 'name']
            },
            42: {
                code: 'RESIDUAL_SOLVENTS_1_COMMENT',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '1', 'comment']
            },
            43: {
                code: 'RESIDUAL_SOLVENTS_1_EQ',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '1', 'eq']
            },
            44: {
                code: 'RESIDUAL_SOLVENTS_1_RANK',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '1', 'name', 'rank']
            },
            45: {
                code: 'RESIDUAL_SOLVENTS_1_NAME',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '1', 'name', 'name']
            },
            46: {
                code: 'RESIDUAL_SOLVENTS_2_COMMENT',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '2', 'comment']
            },
            47: {
                code: 'RESIDUAL_SOLVENTS_2_EQ',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '2', 'eq']
            },
            48: {
                code: 'RESIDUAL_SOLVENTS_2_RANK',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '2', 'name', 'rank']
            },
            49: {
                code: 'RESIDUAL_SOLVENTS_2_NAME',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '2', 'name', 'name']
            },
            50: {
                code: 'RESIDUAL_SOLVENTS_3_COMMENT',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '3', 'comment']
            },
            51: {
                code: 'RESIDUAL_SOLVENTS_3_EQ',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '3', 'eq']
            },
            52: {
                code: 'RESIDUAL_SOLVENTS_3_RANK',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '3', 'name', 'rank']
            },
            53: {
                code: 'RESIDUAL_SOLVENTS_3_NAME',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '3', 'name', 'name']
            },
            54: {
                code: 'RESIDUAL_SOLVENTS_4_COMMENT',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '4', 'comment']
            },
            55: {
                code: 'RESIDUAL_SOLVENTS_4_EQ',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '4', 'eq']
            },
            56: {
                code: 'RESIDUAL_SOLVENTS_4_RANK',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '4', 'name', 'rank']
            },
            57: {
                code: 'RESIDUAL_SOLVENTS_4_NAME',
                name: 'residualSolvents',
                path: ['residualSolvents', 'data', '4', 'name', 'name']
            },
            58: {
                code: 'SOLUBILITY_SOLVENTS_STRING',
                name: 'solubility',
                path: ['solubility', 'asString']
            },
            59: {
                code: 'SOLUBILITY_SOLVENTS_0_COMMENT',
                name: 'solubility',
                path: ['solubility', 'data', '0', 'comment']
            },
            60: {
                code: 'SOLUBILITY_SOLVENTS_0_TYPE',
                name: 'solubility',
                path: ['solubility', 'data', '0', 'type', 'name']
            },
            61: {
                code: 'SOLUBILITY_SOLVENTS_0_RANK',
                name: 'solubility',
                path: ['solubility', 'data', '0', 'solventName', 'rank']
            },
            62: {
                code: 'SOLUBILITY_SOLVENTS_0_NAME',
                name: 'solubility',
                path: ['solubility', 'data', '0', 'solventName', 'name']
            },
            63: {
                code: 'SOLUBILITY_SOLVENTS_0_ENABLE',
                name: 'solubility',
                path: ['solubility', 'data', '0', 'solventName', 'enable']
            },
            64: {
                code: 'SOLUBILITY_SOLVENTS_0_VALUE_NAME',
                name: 'solubility',
                path: ['solubility', 'data', '0', 'value', 'value', 'name']
            },
            65: {
                code: 'SOLUBILITY_SOLVENTS_0_VALUE',
                name: 'solubility',
                path: ['solubility', 'data', '0', 'value', 'value']
            },
            66: {
                code: 'SOLUBILITY_SOLVENTS_0_VALUE_OPERATOR',
                name: 'solubility',
                path: ['solubility', 'data', '0', 'value', 'operator', 'name']
            },
            67: {
                code: 'SOLUBILITY_SOLVENTS_0_VALUE_UNIT',
                name: 'solubility',
                path: ['solubility', 'data', '0', 'value', 'unit', 'name']
            },
            68: {
                code: 'SOLUBILITY_SOLVENTS_1_COMMENT',
                name: 'solubility',
                path: ['solubility', 'data', '1', 'comment']
            },
            69: {
                code: 'SOLUBILITY_SOLVENTS_1_TYPE',
                name: 'solubility',
                path: ['solubility', 'data', '1', 'type', 'name']
            },
            70: {
                code: 'SOLUBILITY_SOLVENTS_1_RANK',
                name: 'solubility',
                path: ['solubility', 'data', '1', 'solventName', 'rank']
            },
            71: {
                code: 'SOLUBILITY_SOLVENTS_1NAME',
                name: 'solubility',
                path: ['solubility', 'data', '1', 'solventName', 'name']
            },
            72: {
                code: 'SOLUBILITY_SOLVENTS_1_ENABLE',
                name: 'solubility',
                path: ['solubility', 'data', '1', 'solventName', 'enable']
            },
            73: {
                code: 'SOLUBILITY_SOLVENTS_1_VALUE_NAME',
                name: 'solubility',
                path: ['solubility', 'data', '1', 'value', 'value', 'name']
            },
            74: {
                code: 'SOLUBILITY_SOLVENTS_1_VALUE',
                name: 'solubility',
                path: ['solubility', 'data', '1', 'value', 'value']
            },
            75: {
                code: 'SOLUBILITY_SOLVENTS_1_VALUE_OPERATOR',
                name: 'solubility',
                path: ['solubility', 'data', '1', 'value', 'operator', 'name']
            },
            76: {
                code: 'SOLUBILITY_SOLVENTS_1_VALUE_UNIT',
                name: 'solubility',
                path: ['solubility', 'data', '1', 'value', 'unit', 'name']
            },
            77: {
                code: 'SOLUBILITY_SOLVENTS_2_COMMENT',
                name: 'solubility',
                path: ['solubility', 'data', '2', 'comment']
            },
            78: {
                code: 'SOLUBILITY_SOLVENTS_2_TYPE',
                name: 'solubility',
                path: ['solubility', 'data', '2', 'type', 'name']
            },
            79: {
                code: 'SOLUBILITY_SOLVENTS_2_RANK',
                name: 'solubility',
                path: ['solubility', 'data', '2', 'solventName', 'rank']
            },
            80: {
                code: 'SOLUBILITY_SOLVENTS_2_NAME',
                name: 'solubility',
                path: ['solubility', 'data', '2', 'solventName', 'name']
            },
            81: {
                code: 'SOLUBILITY_SOLVENTS_2_ENABLE',
                name: 'solubility',
                path: ['solubility', 'data', '2', 'solventName', 'enable']
            },
            82: {
                code: 'SOLUBILITY_SOLVENTS_2_VALUE_NAME',
                name: 'solubility',
                path: ['solubility', 'data', '2', 'value', 'value', 'name']
            },
            83: {
                code: 'SOLUBILITY_SOLVENTS_2_VALUE',
                name: 'solubility',
                path: ['solubility', 'data', '2', 'value', 'value']
            },
            84: {
                code: 'SOLUBILITY_SOLVENTS_2_VALUE_OPERATOR',
                name: 'solubility',
                path: ['solubility', 'data', '2', 'value', 'operator', 'name']
            },
            85: {
                code: 'SOLUBILITY_SOLVENTS_2_VALUE_UNIT',
                name: 'solubility',
                path: ['solubility', 'data', '2', 'value', 'unit', 'name']
            },
            86: {
                code: 'SOLUBILITY_SOLVENTS_3_COMMENT',
                name: 'solubility',
                path: ['solubility', 'data', '3', 'comment']
            },
            87: {
                code: 'SOLUBILITY_SOLVENTS_3_TYPE',
                name: 'solubility',
                path: ['solubility', 'data', '3', 'type', 'name']
            },
            88: {
                code: 'SOLUBILITY_SOLVENTS_3_RANK',
                name: 'solubility',
                path: ['solubility', 'data', '3', 'solventName', 'rank']
            },
            89: {
                code: 'SOLUBILITY_SOLVENTS_3_NAME',
                name: 'solubility',
                path: ['solubility', 'data', '3', 'solventName', 'name']
            },
            90: {
                code: 'SOLUBILITY_SOLVENTS_3_ENABLE',
                name: 'solubility',
                path: ['solubility', 'data', '3', 'solventName', 'enable']
            },
            91: {
                code: 'SOLUBILITY_SOLVENTS_3_VALUE_NAME',
                name: 'solubility',
                path: ['solubility', 'data', '3', 'value', 'value', 'name']
            },
            92: {
                code: 'SOLUBILITY_SOLVENTS_3_VALUE',
                name: 'solubility',
                path: ['solubility', 'data', '3', 'value', 'value']
            },
            93: {
                name: 'solubility',
                path: ['solubility', 'data', '3', 'value', 'operator', 'name']
            },
            94: {
                code: 'SOLUBILITY_SOLVENTS_3_VALUE_UNIT',
                name: 'solubility',
                path: ['solubility', 'data', '3', 'value', 'unit', 'name']
            },
            95: {
                code: 'SOLUBILITY_SOLVENTS_4_COMMENT',
                name: 'solubility',
                path: ['solubility', 'data', '4', 'comment']
            },
            96: {
                code: 'SOLUBILITY_SOLVENTS_4_TYPE',
                name: 'solubility',
                path: ['solubility', 'data', '4', 'type', 'name']
            },
            97: {
                code: 'SOLUBILITY_SOLVENTS_4_RANK',
                name: 'solubility',
                path: ['solubility', 'data', '4', 'solventName', 'rank']
            },
            98: {
                code: 'SOLUBILITY_SOLVENTS_4_NAME',
                name: 'solubility',
                path: ['solubility', 'data', '4', 'solventName', 'name']
            },
            99: {
                code: 'SOLUBILITY_SOLVENTS_4_ENABLE',
                name: 'solubility',
                path: ['solubility', 'data', '4', 'solventName', 'enable']
            },
            100: {
                code: 'SOLUBILITY_SOLVENTS_4_VALUE_NAME',
                name: 'solubility',
                path: ['solubility', 'data', '4', 'value', 'value', 'name']
            },
            101: {
                code: 'SOLUBILITY_SOLVENTS_4_VALUE',
                name: 'solubility',
                path: ['solubility', 'data', '4', 'value', 'value']
            },
            102: {
                code: 'SOLUBILITY_SOLVENTS_4_VALUE_OPERATOR',
                name: 'solubility',
                path: ['solubility', 'data', '4', 'value', 'operator', 'name']
            },
            103: {
                code: 'SOLUBILITY_SOLVENTS_4_VALUE_UNIT',
                name: 'solubility',
                path: ['solubility', 'data', '4', 'value', 'unit', 'name']
            },
            104: {
                code: 'EXTERNAL_SUPPLIER_STRING',
                name: 'externalSupplier',
                path: ['externalSupplier', 'asString']
            },
            105: {
                code: 'EXTERNAL_SUPPLIER_REG_NUMBER',
                name: 'externalSupplier',
                path: ['externalSupplier', 'catalogRegistryNumber']
            },
            106: {
                code: 'EXTERNAL_SUPPLIER_CODE_AND_NAME',
                name: 'externalSupplier',
                path: ['externalSupplier', 'codeAndName', 'name']
            },
            107: {
                code: 'COMPOUND_PROTECTION',
                name: 'compoundProtection',
                path: ['compoundProtection', 'name']
            },
            108: {
                code: 'HEALTH_HAZARDS_STRING',
                name: 'healthHazards',
                path: ['healthHazards', 'asString']
            },
            109: {
                code: 'HEALTH_HAZARDS_0',
                name: 'healthHazards',
                path: ['healthHazards', 'data', '0']
            },
            110: {
                code: 'HEALTH_HAZARDS_1',
                name: 'healthHazards',
                path: ['healthHazards', 'data', '1']
            },
            111: {
                code: 'HEALTH_HAZARDS_2',
                name: 'healthHazards',
                path: ['healthHazards', 'data', '2']
            },
            112: {
                code: 'HEALTH_HAZARDS_3',
                name: 'healthHazards',
                path: ['healthHazards', 'data', '3']
            },
            113: {
                code: 'HEALTH_HAZARDS_4',
                name: 'healthHazards',
                path: ['healthHazards', 'data', '4']
            },
            114: {
                code: 'HEALTH_HAZARDS_5',
                name: 'healthHazards',
                path: ['healthHazards', 'data', '5']
            },
            115: {
                code: 'HANDLING_PRECAUTIONS_STRING',
                name: 'handlingPrecautions',
                path: ['handlingPrecautions', 'asString']
            },
            116: {
                code: 'HANDLING_PRECAUTIONS_0',
                name: 'handlingPrecautions',
                path: ['handlingPrecautions', 'data', '0']
            },
            117: {
                code: 'HANDLING_PRECAUTIONS_1',
                name: 'handlingPrecautions',
                path: ['handlingPrecautions', 'data', '1']
            },
            118: {
                code: 'HANDLING_PRECAUTIONS_2',
                name: 'handlingPrecautions',
                path: ['handlingPrecautions', 'data', '2']
            },
            119: {
                code: 'HANDLING_PRECAUTIONS_3',
                name: 'handlingPrecautions',
                path: ['handlingPrecautions', 'data', '3']
            },
            120: {
                code: 'HANDLING_PRECAUTIONS_4',
                name: 'handlingPrecautions',
                path: ['handlingPrecautions', 'data', '4']
            },
            121: {
                code: 'STORAGE_INSTRUCTIONS_STRING',
                name: 'storageInstructions',
                path: ['storageInstructions', 'asString']
            },
            122: {
                code: 'STORAGE_INSTRUCTIONS_0',
                name: 'storageInstructions',
                path: ['storageInstructions', 'data', '0']
            },
            123: {
                code: 'STORAGE_INSTRUCTIONS_1',
                name: 'storageInstructions',
                path: ['storageInstructions', 'data', '1']
            },
            124: {
                code: 'STORAGE_INSTRUCTIONS_2',
                name: 'storageInstructions',
                path: ['storageInstructions', 'data', '2']
            },
            125: {
                code: 'STORAGE_INSTRUCTIONS_3',
                name: 'storageInstructions',
                path: ['storageInstructions', 'data', '3']
            },
            126: {
                code: 'BATCH_COMMENTS',
                name: 'comments',
                path: ['comments']
            }
        });
